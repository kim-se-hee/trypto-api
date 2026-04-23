import { useEffect, useState } from "react";
import { Copy, Check, Loader2 } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { CoinIcon } from "@/components/market/CoinIcon";
import { getDepositAddress } from "@/lib/api/wallet-api";
import type { WalletCoinBalance } from "@/lib/types/wallet";

interface DepositModalProps {
  isOpen: boolean;
  onClose: () => void;
  coin: WalletCoinBalance;
  walletId: number;
}

export function DepositModal({
  isOpen,
  onClose,
  coin,
  walletId,
}: DepositModalProps) {
  const [address, setAddress] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!isOpen || !coin.coinId) return;

    setLoading(true);
    setError(null);
    setAddress(null);

    getDepositAddress(walletId, coin.coinId)
      .then((res) => setAddress(res.address))
      .catch(() => setError("입금 주소를 불러올 수 없습니다."))
      .finally(() => setLoading(false));
  }, [isOpen, walletId, coin.coinId]);

  async function handleCopy(text: string) {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // 클립보드 접근 실패
    }
  }

  function handleOpenChange(open: boolean) {
    if (!open) {
      onClose();
      setAddress(null);
      setError(null);
      setCopied(false);
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogContent showCloseButton className="max-w-md gap-0 p-0">
        <DialogHeader className="border-b border-border/30 px-6 py-5">
          <div className="flex items-center gap-3">
            <CoinIcon symbol={coin.coinSymbol} size={32} />
            <div>
              <DialogTitle>{coin.coinSymbol} 입금</DialogTitle>
              <DialogDescription className="mt-0.5">
                {coin.coinName}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="space-y-5 px-6 py-5">
          {loading && (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          )}

          {error && (
            <p className="text-center text-sm text-destructive">{error}</p>
          )}

          {address && (
            <div className="space-y-2">
              <label className="text-sm font-medium">입금 주소</label>
              <div className="rounded-lg border border-border/50 bg-secondary/20 px-3 py-2.5">
                <p className="break-all font-mono text-xs leading-relaxed">
                  {address}
                </p>
              </div>
              <Button
                type="button"
                variant="outline"
                className="w-full gap-2"
                onClick={() => handleCopy(address)}
              >
                {copied ? (
                  <>
                    <Check className="h-4 w-4 text-emerald-500" />
                    복사됨
                  </>
                ) : (
                  <>
                    <Copy className="h-4 w-4" />
                    주소 복사
                  </>
                )}
              </Button>
            </div>
          )}

          <Button className="w-full" onClick={onClose}>
            확인
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
