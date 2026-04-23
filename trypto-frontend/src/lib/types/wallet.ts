export interface WalletCoinBalance {
  coinId: number | null;
  coinSymbol: string;
  coinName: string;
  available: number;
  locked: number;
  currentPrice: number;
}

export interface WalletData {
  walletId: number;
  exchangeId: string;
  exchangeName: string;
  baseCurrency: string;
  balances: WalletCoinBalance[];
}

export interface TransferRecord {
  id: string;
  exchangeId: string;
  type: "DEPOSIT" | "WITHDRAW";
  asset: string;
  amount: number;
  fromExchangeName: string;
  toExchangeName: string;
  status: "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED" | "RETURNED" | "DELAYED";
  requestedAt: string;
  completedAt?: string;
}
