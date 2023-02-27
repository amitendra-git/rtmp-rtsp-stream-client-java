package com.github.faucamp.simplertmp.packets;

import com.github.faucamp.simplertmp.amf.AmfData;
import com.github.faucamp.simplertmp.amf.AmfNumber;
import com.github.faucamp.simplertmp.amf.AmfString;
import com.github.faucamp.simplertmp.io.ChunkStreamInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Encapsulates an command/"invoke" RTMP packet
 *
 * Invoke/command packet structure (AMF encoded):
 * (String) <commmand name>
 * (Number) <Transaction ID>
 * (Mixed) <Argument> ex. Null, String, Object: {key1:value1, key2:value2 ... }
 *
 * @author francois
 */
public class Command extends VariableBodyRtmpPacket {

  private static final String TAG = "Command";

  private String commandName;
  private int transactionId;

  public Command(RtmpHeader header) {
    super(header);
  }

  public Command(String commandName, int transactionId, ChunkStreamInfo channelInfo) {
    super(new RtmpHeader((channelInfo.canReusePrevHeaderTx(RtmpHeader.MessageType.COMMAND_AMF0)
        ? RtmpHeader.ChunkType.TYPE_1_RELATIVE_LARGE : RtmpHeader.ChunkType.TYPE_0_FULL),
        ChunkStreamInfo.RTMP_CID_OVER_CONNECTION, RtmpHeader.MessageType.COMMAND_AMF0));
    this.commandName = commandName;
    this.transactionId = transactionId;
  }

  public Command(String commandName, int transactionId) {
    super(new RtmpHeader(RtmpHeader.ChunkType.TYPE_0_FULL, ChunkStreamInfo.RTMP_CID_OVER_CONNECTION,
        RtmpHeader.MessageType.COMMAND_AMF0));
    this.commandName = commandName;
    this.transactionId = transactionId;
  }

  public String getCommandName() {
    return commandName;
  }

  public void setCommandName(String commandName) {
    this.commandName = commandName;
  }

  public int getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(int transactionId) {
    this.transactionId = transactionId;
  }

  @Override
  public void readBody(InputStream in) throws IOException {
    readVariableData(in, 0);
    List<AmfData> dataList = getData();
    AmfString amfString = (AmfString) dataList.remove(0);
    commandName = amfString.getValue();
    if (!dataList.isEmpty() && dataList.get(0) instanceof AmfNumber) {
      AmfNumber amfNumber = (AmfNumber) dataList.remove(0);
      transactionId = (int) amfNumber.getValue();
    } else {
      transactionId = 0;
    }
  }

  @Override
  protected void writeBody(OutputStream out) throws IOException {
    AmfString.writeStringTo(out, commandName, false);
    AmfNumber.writeNumberTo(out, transactionId);
    // Write body data
    writeVariableData(out);
  }

  @Override
  protected byte[] array() {
    return null;
  }

  @Override
  protected int size() {
    return 0;
  }

  @Override
  public String toString() {
    return "RTMP Command (command: " + commandName + ", transaction ID: " + transactionId + ")";
  }
}
