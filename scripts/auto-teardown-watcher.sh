#!/usr/bin/env bash
# 부하테스트 완료 후 자동 teardown 워처 (세션 내 백그라운드용)
# k6 프로세스가 사라지면 /loadtest-down 과 동일한 AWS terminate 수행
set -u

KEY="${SSH_KEY:-$HOME/.ssh/trypto-key-pair.pem}"
SSH_OPTS="-i $KEY -o StrictHostKeyChecking=accept-new -o UserKnownHostsFile=/dev/null -o LogLevel=ERROR -o ConnectTimeout=10"
LG_IP="${1:?loadgen ip required}"
REGION="ap-northeast-2"

echo "[watcher] polling k6 on $LG_IP every 30s"

# k6 프로세스가 떠 있는 동안 대기. 시작 직후라 아직 안 떴을 수 있으니 먼저 2분간 기동 확인.
STARTUP_DEADLINE=$(( $(date +%s) + 120 ))
while [ $(date +%s) -lt $STARTUP_DEADLINE ]; do
  if ssh $SSH_OPTS ubuntu@"$LG_IP" 'pgrep -x k6 >/dev/null' 2>/dev/null; then
    break
  fi
  sleep 5
done

# 본 폴링: k6 없어질 때까지 대기
while ssh $SSH_OPTS ubuntu@"$LG_IP" 'pgrep -x k6 >/dev/null' 2>/dev/null; do
  sleep 30
done

echo "[watcher] k6 finished. running teardown."

IDS=$(aws ec2 describe-instances \
  --region "$REGION" \
  --filters "Name=tag:Project,Values=trypto-loadtest" \
            "Name=instance-state-name,Values=pending,running,stopping,stopped" \
  --query 'Reservations[].Instances[].InstanceId' \
  --output text)

if [ -z "$IDS" ]; then
  echo "[watcher] 정리할 인스턴스 없음"
  exit 0
fi

echo "[watcher] terminating: $IDS"
aws ec2 terminate-instances --region "$REGION" --instance-ids $IDS
echo "[watcher] done. EIP 는 보존됨."
