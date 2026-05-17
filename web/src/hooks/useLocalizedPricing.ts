import { useState, useEffect } from 'react';

// Free tier exchange rate API
const EXCHANGE_RATE_API = 'https://open.er-api.com/v6/latest/USD';
// Free IP to location API (includes currency code)
const IP_API = 'https://ipapi.co/json/';

interface PricingData {
  currency: string;
  rate: number;
  loading: boolean;
  error: string | null;
}

export function useLocalizedPricing() {
  const [data, setData] = useState<PricingData>({
    currency: 'USD',
    rate: 1,
    loading: true,
    error: null,
  });

  useEffect(() => {
    async function fetchPricingInfo() {
      try {
        // 1. Get user's local currency based on IP
        const ipRes = await fetch(IP_API);
        const ipData = await ipRes.json();
        const userCurrency = ipData.currency || 'USD';

        // 2. Get exchange rates relative to USD
        const ratesRes = await fetch(EXCHANGE_RATE_API);
        const ratesData = await ratesRes.json();
        
        const rate = ratesData.rates[userCurrency] || 1;

        setData({
          currency: userCurrency,
          rate: rate,
          loading: false,
          error: null,
        });
      } catch (err) {
        console.error("Failed to fetch localized pricing", err);
        // Fallback to USD
        setData({
          currency: 'USD',
          rate: 1,
          loading: false,
          error: 'Failed to detect location',
        });
      }
    }

    fetchPricingInfo();
  }, []);

  // Format a base USD price into the user's local currency
  const formatPrice = (basePriceUSD: number) => {
    if (data.loading) return '...';
    
    const convertedAmount = basePriceUSD * data.rate;
    
    try {
      return new Intl.NumberFormat(navigator.language, {
        style: 'currency',
        currency: data.currency || 'USD',
        maximumFractionDigits: 0, // Keep it clean (e.g. $45 instead of $45.00)
      }).format(convertedAmount);
    } catch (e) {
      console.warn("Intl formatting failed for currency:", data.currency, e);
      return `${data.currency || 'USD'} ${convertedAmount.toFixed(0)}`;
    }
  };

  return { ...data, formatPrice };
}
