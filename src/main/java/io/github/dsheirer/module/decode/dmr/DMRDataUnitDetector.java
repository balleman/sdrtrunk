/*
 * ******************************************************************************
 * sdrtrunk
 * Copyright (C) 2014-2019 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * *****************************************************************************
 */
package io.github.dsheirer.module.decode.dmr;

import io.github.dsheirer.bits.BitSetFullException;
import io.github.dsheirer.bits.CorrectedBinaryMessage;
import io.github.dsheirer.dsp.psk.pll.IPhaseLockedLoop;
import io.github.dsheirer.dsp.symbol.Dibit;
import io.github.dsheirer.module.decode.dmr.message.DMRMessage;
import io.github.dsheirer.module.decode.dmr.message.DMRMessageFactory;
import io.github.dsheirer.module.decode.dmr.message.data.DataMessage;
import io.github.dsheirer.module.decode.dmr.message.data.DataType;
import io.github.dsheirer.module.decode.dmr.message.data.SlotType;
import io.github.dsheirer.sample.Listener;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMRDataUnitDetector implements Listener<Dibit>, IDMRSyncDetectListener
{
    private final static Logger mLog = LoggerFactory.getLogger(DMRDataUnitDetector.class);
    private static final int DATA_UNIT_DIBIT_LENGTH = 144; //132 dibits + 12 dibits(CACHs)
    private static final int SYNC_DIBIT_LENGTH = 24;
    private static final int MAXIMUM_SYNC_MATCH_BIT_ERRORS = 9;
    private DMRSyncDetector mSyncDetector;
    private DibitDelayBuffer mSyncDelayBuffer = new DibitDelayBuffer(DATA_UNIT_DIBIT_LENGTH);
    private IDMRDataUnitDetectListener mDataUnitDetectListener;
    private boolean mInitialSyncTestProcessed = false;
    private int mDibitsProcessed = 0;

    public DMRDataUnitDetector(IDMRDataUnitDetectListener dataUnitDetectListener, IPhaseLockedLoop phaseLockedLoop)
    {
        mDataUnitDetectListener = dataUnitDetectListener;
        mSyncDetector = new DMRSyncDetector(this, phaseLockedLoop);
    }

    /**
     * Sets the sample rate for the phase inversion sync detector
     */
    public void setSampleRate(double sampleRate)
    {
        //mSyncDetector.setSampleRate(sampleRate);
    }

    public void reset()
    {
        mDibitsProcessed = 0;
        mInitialSyncTestProcessed = false;
    }

    @Override
    public void syncDetected(int bitErrors)
    {
        //not used
    }

    @Override
    public void syncDetected(int bitErrors, DMRSyncPattern pattern) {
        if(pattern == DMRSyncPattern.BASE_STATION_DATA || pattern == DMRSyncPattern.MOBILE_STATION_DATA || pattern == DMRSyncPattern.DIRECT_MODE_DATA_TIMESLOT_1) {
            parseDataFrame(bitErrors, pattern);
        } else {
            mLog.debug("VOICE FRAME");
        }
        mDibitsProcessed = 0;
        mInitialSyncTestProcessed = true;
    }

    @Override
    public void syncLost()
    {
        dispatchSyncLoss(0);
    }

    private void dispatchSyncLoss(int bitsProcessed)
    {
        if(mDataUnitDetectListener != null)
        {
            mDataUnitDetectListener.syncLost(bitsProcessed);
        }
    }

    @Override
    public void receive(Dibit dibit)
    {
        mDibitsProcessed++;

        //Broadcast a sync loss every 4800 dibits/9600 bits ... or 1x per second for phase 1
        if(mDibitsProcessed > 4864)
        {
            dispatchSyncLoss(9600);
            mDibitsProcessed -= 4800;
        }

        mSyncDetector.receive(mSyncDelayBuffer.getAndPut(dibit));
        /*
        if(dibit == Dibit.D01_PLUS_3){
            System.out.print("1");
        }
        else if(dibit == Dibit.D11_MINUS_3) {
            System.out.print("3");
        }
        */
        /*
        //If the sync detector doesn't fire and we've processed enough dibits for a sync/nid sequence
        //immediately following a valid message, then test for a NID anyway ... maybe the sync was corrupted
        if(!mInitialSyncTestProcessed && mDibitsProcessed == DATA_UNIT_DIBIT_LENGTH)
        {
            mInitialSyncTestProcessed = true;
            checkForNid(mSyncDetector.getPrimarySyncMatchErrorCount(), true);
        }
         */
    }
    private void parseDataFrame(int bitErrorCount, DMRSyncPattern pattern)
    {
        if(bitErrorCount <= MAXIMUM_SYNC_MATCH_BIT_ERRORS)
        {
            Dibit[] db = mSyncDelayBuffer.getBuffer();
            CorrectedBinaryMessage mesg = new CorrectedBinaryMessage(288);
            for(int i = 0; i < db.length; i++) {
                try {
                    mesg.add(db[i].getBit1());
                    mesg.add(db[i].getBit2());
                } catch (BitSetFullException e) {
                    e.printStackTrace();
                }
            }
            SlotType sl = new SlotType(mesg);
            DataMessage msg = DMRMessageFactory.createDataMessage(sl.getDataType(),pattern, 0, mesg);
            if(mDataUnitDetectListener != null)
            {
                //mPreviousDataUnitId = getDataUnitID(correctedNid);
                mDataUnitDetectListener.dataUnitDetected(msg, (bitErrorCount));
            }
        }
    }

    /**
     * Determines the data unit ID present in the nid value.
     * @param nid in reverse bit order
     * @return
     */
    public DataType getDataUnitID(int[] nid)
    {
        return DataType.fromValue(0);
    }

    /**
     * Circular buffer for storing and accessing dibits.
     */
    public class DibitDelayBuffer
    {
        protected Dibit[] mBuffer;
        protected int mPointer;

        /**
         * Constructs a dibit delay buffer of the specified length
         */
        public DibitDelayBuffer(int length)
        {
            mBuffer = new Dibit[length];

            //Preload the buffer to avoid null pointers
            for(int x = 0; x < length; x++)
            {
                mBuffer[x] = Dibit.D00_PLUS_1;
            }
        }
        public int[] getNID()
        {
            System.out.print("Current Location: " + mPointer);
            return null;
        }

        public void log()
        {

        }

        /**
         * Returns an ordered buffer of the internal circular buffer contents.
         */
        public Dibit[] getBuffer()
        {
            Dibit[] transferBuffer = new Dibit[mBuffer.length];

            int transferBufferPointer = 0;
            int bufferPointer = mPointer;

            while(transferBufferPointer < transferBuffer.length)
            {
                transferBuffer[transferBufferPointer++] = mBuffer[bufferPointer++];

                if(bufferPointer >= mBuffer.length)
                {
                    bufferPointer = 0;
                }
            }

            return transferBuffer;
        }

        public int[] getBufferAsArray()
        {
            Dibit[] dibits = getBuffer();

            int[] bits = new int[dibits.length * 2];

            for(int x = 0; x < dibits.length; x++)
            {
                if(dibits[x].getBit1())
                {
                    bits[x * 2] = 1;
                }
                if(dibits[x].getBit2())
                {
                    bits[x * 2 + 1] = 1;
                }
            }

            return bits;
        }

        /**
         * Places the dibit into the internal circular buffer, overwriting the oldest dibit.
         */
        public void put(Dibit dibit)
        {
            mBuffer[mPointer++] = dibit;

            if(mPointer >= mBuffer.length)
            {
                mPointer = 0;
            }
        }

        /**
         * Places the dibit into the internal circular buffer, overwriting and returning the
         * oldest dibit.
         */
        public Dibit getAndPut(Dibit dibit)
        {
            //      lSYNC...24...rSYNC......54.......mP
            // CACH(24)...PAYLOAD I(108)...SYNC(48)...PAYLOAD II(108)
            //                                                     ^
            int p = mPointer - 54;
            if(p<0) {
                p += mBuffer.length;
            }
            Dibit toReturn = mBuffer[p];
            put(dibit);
            return toReturn;
        }
    }

    public static int[] reverse(int[] values)
    {
        int[] reversed = new int[values.length];

        for(int x = 0; x < values.length; x++)
        {
            reversed[values.length - x - 1] = values[x];
        }

        return reversed;
    }

    public static void logNID(int[] nid, boolean corrected)
    {

    }

    public static int getBitErrorCount(int[] a, int[] b)
    {
        Validate.isTrue(a.length == b.length, "Array lengths must be the same");

        int count = 0;

        for(int x = 0; x < a.length; x++)
        {
            if(a[x] != b[x])
            {
                count++;
            }
        }

        return count;
    }
}