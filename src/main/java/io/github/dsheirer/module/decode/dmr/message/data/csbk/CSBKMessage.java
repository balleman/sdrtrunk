/*
 * ******************************************************************************
 * sdrtrunk
 * Copyright (C) 2014-2020 Dennis Sheirer, Zhenyu Mao
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
package io.github.dsheirer.module.decode.dmr.message.data.csbk;

import io.github.dsheirer.bits.BinaryMessage;
import io.github.dsheirer.bits.CorrectedBinaryMessage;
import io.github.dsheirer.edac.CRCDMR;
import io.github.dsheirer.module.decode.dmr.DMRSyncPattern;
import io.github.dsheirer.module.decode.dmr.message.CACH;
import io.github.dsheirer.module.decode.dmr.message.data.DataMessage;
import io.github.dsheirer.module.decode.dmr.message.data.SlotType;
import io.github.dsheirer.module.decode.dmr.message.type.Vendor;

/**
 * Control Signalling Block (CSBK) Message
 *
 * ETSI 102 361-1 7.2.0
 */
public abstract class CSBKMessage extends DataMessage
{
    private static final int LAST_BLOCK = 0;
    private static final int PROTECT_FLAG = 1;
    private static final int[] OPCODE = new int[]{2, 3, 4, 5, 6, 7};
    private static final int[] VENDOR = new int[]{8, 9, 10, 11, 12, 13, 14, 15};

    /**
     * Constructs an instance
     * @param syncPattern for the CSBK
     * @param message bits
     * @param cach for the DMR burst
     * @param slotType for this message
     * @param timestamp
     * @param timeslot
     */
    public CSBKMessage(DMRSyncPattern syncPattern, CorrectedBinaryMessage message, CACH cach, SlotType slotType,
                       long timestamp, int timeslot)
    {
        super(syncPattern, message, cach, slotType, timestamp, timeslot);

        int correctedBitCount = CRCDMR.correctCCITT80(message, 0, 80, 0xA5A5);

        //Set message valid flag according to the corrected bit count for the CRC protected message
        setValid(correctedBitCount < 2);
    }

    public boolean isLastBlock()
    {
        return getMessage().get(LAST_BLOCK);
    }

    public boolean isEncrypted()
    {
        return getMessage().get(PROTECT_FLAG);
    }

    /**
     * Utility method to lookup the opcode from a CSBK message
     * @param message containing CSBK bits
     * @return opcode
     */
    public static Opcode getOpcode(BinaryMessage message)
    {
        return Opcode.fromValue(message.getInt(OPCODE), getVendor(message));
    }

    /**
     * Opcode for this CSBK message
     */
    public Opcode getOpcode()
    {
        return getOpcode(getMessage());
    }

    /**
     * Opcode numeric value
     */
    protected int getOpcodeValue()
    {
        return getMessage().getInt(OPCODE);
    }

    /**
     * Utility method to lookup the vendor from a CSBK message
     * @param message containing CSBK bits
     * @return vendor
     */
    public static Vendor getVendor(BinaryMessage message)
    {
        return Vendor.fromValue(message.getInt(VENDOR));
    }

    /**
     * Vendor for this message
     */
    public Vendor getVendor()
    {
        return getVendor(getMessage());
    }

    /**
     * Numerical value for the vendor
     */
    protected int getVendorID()
    {
        return getMessage().getInt(VENDOR);
    }
}
