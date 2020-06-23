package io.github.dsheirer.module.decode.dmr;

import io.github.dsheirer.dsp.symbol.Dibit;
import io.github.dsheirer.dsp.symbol.QPSKCarrierLock;
import org.apache.commons.lang3.Validate;

/**
 * Detector for DMR sync patterns that also includes built-in support for detecting PLL phase lock errors.
 */
public class DMRSyncDetector
{
    private static long SYNC_MASK = 0xFFFFFFFFFFFFl;

    private IDMRSyncDetectListener mSyncDetectListener;
    private int mMaxBitErrors;
    private long mCurrentSyncValue;
    private long mErrorPattern;
    private int mPatternMatchBitErrorCount;

    /**
     * Constructs an instance
     * @param maxBitErrors allowed for matching any of the sync patterns
     */
    public DMRSyncDetector(IDMRSyncDetectListener listener, int maxBitErrors)
    {
        Validate.notNull(listener, "Sync detector cannot be null");
        Validate.inclusiveBetween(0, 24, maxBitErrors,
            "Max (allowable) bit errors for sync match must be between 0 and 24");
        mSyncDetectListener = listener;
        mMaxBitErrors = maxBitErrors;
    }

    /**
     * Processes the dibit.
     * @param dibit to process
     * @return true if a sync was detected.
     */
    public boolean hasSync(Dibit dibit)
    {
        mCurrentSyncValue = Long.rotateLeft(mCurrentSyncValue, 2);
        mCurrentSyncValue &= SYNC_MASK;
        mCurrentSyncValue += dibit.getValue();
        return hasSync();
    }

    /**
     * Checks the current sync value against each of the sync patterns to determine if the value matches a pattern.
     */
    public boolean hasSync()
    {
        for(DMRSyncPattern pattern: DMRSyncPattern.SYNC_PATTERNS)
        {
            mErrorPattern = mCurrentSyncValue ^ pattern.getPattern();

            if(mErrorPattern == 0)
            {
                mSyncDetectListener.syncDetected(pattern, QPSKCarrierLock.NORMAL, 0);
                return true;
            }

            mPatternMatchBitErrorCount = Long.bitCount(mErrorPattern);

            if(mPatternMatchBitErrorCount <= mMaxBitErrors)
            {
                mSyncDetectListener.syncDetected(pattern, QPSKCarrierLock.NORMAL, mPatternMatchBitErrorCount);
                return true;
            }

            //For PLL mis-aligned lock patterns, allow only exact pattern matches
            if((mCurrentSyncValue ^ pattern.getPlus90Pattern()) == 0)
            {
                mSyncDetectListener.syncDetected(pattern, QPSKCarrierLock.PLUS_90, 0);
                return true;
            }

            if((mCurrentSyncValue ^ pattern.getMinus90Pattern()) == 0)
            {
                mSyncDetectListener.syncDetected(pattern, QPSKCarrierLock.MINUS_90, 0);
                return true;
            }

            if((mCurrentSyncValue ^ pattern.getInvertedPattern()) == 0)
            {
                mSyncDetectListener.syncDetected(pattern, QPSKCarrierLock.INVERTED, 0);
                return true;
            }
        }

        return false;
    }

    /**
     * Sets the current sync value to the argument value.
     * @param value to load as the current sync value
     */
    public void setCurrentSyncValue(long value)
    {
        mCurrentSyncValue = value;
    }
}
