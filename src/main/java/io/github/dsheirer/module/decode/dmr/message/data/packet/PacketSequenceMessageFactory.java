/*
 * *****************************************************************************
 *  Copyright (C) 2014-2020 Dennis Sheirer
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
 * ****************************************************************************
 */

package io.github.dsheirer.module.decode.dmr.message.data.packet;

import io.github.dsheirer.bits.BitSetFullException;
import io.github.dsheirer.bits.CorrectedBinaryMessage;
import io.github.dsheirer.message.IMessage;
import io.github.dsheirer.module.decode.dmr.message.data.block.DataBlock;
import io.github.dsheirer.module.decode.dmr.message.data.header.HeaderMessage;
import io.github.dsheirer.module.decode.dmr.message.data.header.PacketSequenceHeader;
import io.github.dsheirer.module.decode.dmr.message.data.header.ProprietaryDataHeader;
import io.github.dsheirer.module.decode.dmr.message.data.header.motorola.MNISProprietaryDataHeader;
import io.github.dsheirer.module.decode.dmr.message.type.ApplicationType;
import io.github.dsheirer.module.decode.ip.ars.ARSPacket;
import io.github.dsheirer.module.decode.ip.lrrp.LRRPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a DMR packet sequence into a message
 */
public class PacketSequenceMessageFactory
{
    private final static Logger mLog = LoggerFactory.getLogger(PacketSequenceMessageFactory.class);

    /**
     * Creates a message from a packet sequence
      * @param packetSequence with message parts
     * @return message
     */
    public static IMessage create(PacketSequence packetSequence)
    {
        if(packetSequence != null && packetSequence.isComplete())
        {
            PacketSequenceHeader primaryHeader = packetSequence.getPacketSequenceHeader();
            CorrectedBinaryMessage packet = getPacket(packetSequence, primaryHeader.isConfirmedData());
            HeaderMessage secondaryHeader = packetSequence.getProprietaryDataHeader();

            if(secondaryHeader instanceof MNISProprietaryDataHeader)
            {
                ApplicationType applicationType = ((MNISProprietaryDataHeader)secondaryHeader).getApplicationType();

                switch(applicationType)
                {
                    case AUTOMATIC_REGISTRATION_SERVICE:
                        return new DMRPacketMessage(packetSequence, new ARSPacket(packet, 0),
                            packet,packetSequence.getTimeslot(),
                            packetSequence.getPacketSequenceHeader().getTimestamp());
                    case LOCATION_REQUEST_RESPONSE_PROTOCOL:
                        return new DMRPacketMessage(packetSequence, new LRRPPacket(packet, 0), packet,
                            packetSequence.getTimeslot(),
                            packetSequence.getPacketSequenceHeader().getTimestamp());
                }
            }
        }

        //TODO: NOTE: Don't create a DMRPacketMessage unless we have a)Packet and b)Packet Sequence Header

        return null;
    }

    /**
     * Extracts and reassembles the packet contents from a Confirmed or Unconfirmed packet sequence.
     * @param sequence that contains a Header and Data Blocks
     * @param confirmed is true or unconfirmed is false
     * @return the extracted packet or null.
     */
    public static CorrectedBinaryMessage getPacket(PacketSequence sequence, boolean confirmed)
    {
        List<CorrectedBinaryMessage> fragments = new ArrayList<>();
        int length = 0;

        if(sequence.hasProprietaryDataHeader())
        {
            CorrectedBinaryMessage prefix = ((ProprietaryDataHeader)sequence.getProprietaryDataHeader()).getPacketPrefix();

            if(prefix != null)
            {
                fragments.add(prefix);
                length += prefix.size();
            }
        }

        for(DataBlock dataBlock: sequence.getDataBlocks())
        {
            //TODO: check data block sequence numbers for confirmed payloads
            CorrectedBinaryMessage fragment = (confirmed ? dataBlock.getConfirmedPayload() : dataBlock.getUnConfirmedPayload());
            fragments.add(fragment);
            length += fragment.size();
        }

        if(length > 0)
        {
            CorrectedBinaryMessage packet = new CorrectedBinaryMessage(length);

            for(CorrectedBinaryMessage fragment: fragments)
            {
                for(int x = 0; x < fragment.size(); x++)
                {
                    try
                    {
                        packet.add(fragment.get(x));
                    }
                    catch(BitSetFullException bsfe)
                    {
                        //We should never get here
                        mLog.error("BitSet full while assembling packet fragments");
                        return packet;
                    }
                }
            }

            mLog.info("Packet: " + packet.size() + " " + packet.toHexString() + " " + packet.toString());

            return packet;
        }

        return null;
    }
}
