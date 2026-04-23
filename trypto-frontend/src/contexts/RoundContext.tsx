import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { InvestmentRound } from "@/lib/types/round";
import { useAuth } from "@/contexts/AuthContext";
import {
  chargeEmergencyFunding as chargeEmergencyFundingApi,
  createIdempotencyKey,
  createRound as createRoundApi,
  fetchActiveRound,
  type CreateRoundParams,
} from "@/lib/api/round-api";

interface RoundContextValue {
  activeRound: InvestmentRound | null;
  hasActiveRound: boolean;
  isRoundLoading: boolean;
  createRound: (params: CreateRoundParams) => Promise<InvestmentRound | null>;
  clearRound: () => void;
  refreshActiveRound: () => Promise<void>;
  chargeEmergencyFunding: (amount: number, exchangeId: number) => Promise<boolean>;
  getWalletId: (exchangeId: number) => number | null;
}

const RoundContext = createContext<RoundContextValue | null>(null);

export function RoundProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [activeRound, setActiveRound] = useState<InvestmentRound | null>(null);
  const [isRoundLoading, setIsRoundLoading] = useState(false);

  const refreshActiveRound = useCallback(async () => {
    if (!user) {
      setActiveRound(null);
      return;
    }

    setIsRoundLoading(true);
    try {
      const round = await fetchActiveRound(user.userId);
      setActiveRound(round);
    } catch (error) {
      console.error("Failed to load active round", error);
      setActiveRound(null);
    } finally {
      setIsRoundLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void refreshActiveRound();
  }, [refreshActiveRound]);

  const createRound = useCallback(async (params: CreateRoundParams): Promise<InvestmentRound | null> => {
    try {
      const round = await createRoundApi(params);
      setActiveRound(round);
      return round;
    } catch (error) {
      console.error("Failed to create round", error);
      return null;
    }
  }, []);

  const clearRound = useCallback(() => {
    setActiveRound(null);
  }, []);

  const getWalletId = useCallback(
    (exchangeId: number): number | null => {
      if (!activeRound) return null;
      const wallet = activeRound.wallets.find((w) => w.exchangeId === exchangeId);
      return wallet?.walletId ?? null;
    },
    [activeRound],
  );

  const chargeEmergencyFunding = useCallback(
    async (amount: number, exchangeId: number): Promise<boolean> => {
      if (!activeRound || !user) return false;
      if (activeRound.status !== "ACTIVE") return false;
      if (activeRound.emergencyChargeCount <= 0) return false;
      if (amount <= 0 || amount > activeRound.emergencyFundingLimit) return false;

      try {
        const result = await chargeEmergencyFundingApi({
          roundId: activeRound.roundId,
          userId: user.userId,
          exchangeId,
          amount,
          idempotencyKey: createIdempotencyKey(),
        });

        setActiveRound((prev) => {
          if (!prev) return prev;
          return {
            ...prev,
            emergencyChargeCount: result.remainingChargeCount,
          };
        });

        return true;
      } catch (error) {
        console.error("Failed to charge emergency funding", error);
        return false;
      }
    },
    [activeRound, user],
  );

  const value = useMemo(
    () => ({
      activeRound,
      hasActiveRound: activeRound !== null,
      isRoundLoading,
      createRound,
      clearRound,
      refreshActiveRound,
      chargeEmergencyFunding,
      getWalletId,
    }),
    [activeRound, isRoundLoading, createRound, clearRound, refreshActiveRound, chargeEmergencyFunding, getWalletId],
  );

  return <RoundContext.Provider value={value}>{children}</RoundContext.Provider>;
}

export function useRound(): RoundContextValue {
  const ctx = useContext(RoundContext);
  if (!ctx) throw new Error("useRound must be used within RoundProvider");
  return ctx;
}
