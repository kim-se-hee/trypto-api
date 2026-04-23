locals {
  sut_user_data = <<-EOT
    #!/bin/bash
    set -eux
    exec > >(tee /var/log/user-data.log) 2>&1

    export DEBIAN_FRONTEND=noninteractive
    apt-get update
    apt-get install -y ca-certificates curl git jq

    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu jammy stable" > /etc/apt/sources.list.d/docker.list
    apt-get update
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    # Disable containerd-snapshotter so Docker uses classic overlay2 storage.
    # cadvisor's Docker factory requires /var/lib/docker/image/overlay2/layerdb,
    # which doesn't exist under containerd-snapshotter mode.
    mkdir -p /etc/docker
    echo '{"features":{"containerd-snapshotter":false}}' > /etc/docker/daemon.json
    systemctl enable docker
    systemctl restart docker
    usermod -aG docker ubuntu

    # App lifecycle(compose up/down, 이미지 pull)는 reset-sut.sh 가 단독 소유.
    # user-data 는 Docker 설치 + 레포 위치만 보장하고 손 뗀다.
    sudo -u ubuntu git clone ${var.infra_repo_url} /home/ubuntu/trypto

    touch /home/ubuntu/READY
    chown ubuntu:ubuntu /home/ubuntu/READY
  EOT
}

resource "aws_instance" "sut" {
  ami                         = data.aws_ami.ubuntu_2204.id
  instance_type               = var.sut_instance_type
  key_name                    = var.key_pair_name
  vpc_security_group_ids      = [aws_security_group.sut.id]
  subnet_id                   = tolist(data.aws_subnets.default.ids)[0]
  associate_public_ip_address = true

  root_block_device {
    volume_size = var.sut_ebs_size_gb
    volume_type = "gp3"
  }

  user_data = local.sut_user_data

  tags = merge(var.tags, { Name = "trypto-sut" })
}
