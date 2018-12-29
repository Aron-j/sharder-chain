package org.conch.consensus.poc.tx;

import com.google.common.collect.Lists;
import org.conch.common.ConchException;
import org.conch.consensus.poc.hardware.SystemInfo;
import org.conch.peer.Peer;
import org.conch.tx.Attachment;
import org.conch.tx.TransactionType;
import org.conch.util.Convert;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:xy@sharder.org">Ben</a>
 * @since 2018/12/12
 */
public interface PocTxBody  {

     enum WeightTableOptions {
        NODE("node"),
        SERVER_OPEN("serverOpen"),
        SS_HOLD("ssHold"),
        HARDWARE_CONFIG("hardwareConfig"),
        NETWORK_CONFIG("networkConfig"),
        TX_HANDLE_PERFORMANCE("txHandlePerformance"),
        BLOCK_MISS("blockMiss"),
        BOC_SPEED("bocSpeed"), // 分叉收敛速度
        ONLINE_RATE("onlineRate");
        
        private String optionValue;

        WeightTableOptions(String optionValue) {
            this.optionValue = optionValue;
        }

        public String getOptionValue() {
            return optionValue;
        }
    }

    enum DeviceLevels {
        POOR(0), // 极差
        BAD(1), // 可以认为是差、低、丢失量高,等
        MIDDLE(2), // 中
        GOOD(3); // 可以认为是好、高、丢失量低,等
        
        private Integer level;

        public Integer getLevel() {
            return level;
        }

        DeviceLevels(Integer level) {
            this.level = level;
        }
    }



     final class PocNodeType extends Attachment.TxBodyBase {
        private String ip;
        private Peer.Type type;

        public String getIp() {
            return ip;
        }

        public Peer.Type getType() {
            return type;
        }

        public PocNodeType(String ip, Peer.Type type) {
          this.ip = ip;
          this.type = type;
        }


        public PocNodeType(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.type = Peer.Type.getByCode(buffer.getInt());
            try {
                this.ip = Convert.readString(buffer,buffer.getShort(),10);
            } catch (ConchException.NotValidException e) {
                e.printStackTrace();
            }
        }

        public PocNodeType(JSONObject attachmentData) {
          super(attachmentData);
          this.ip = (String) attachmentData.get("ip");
          this.type =  Peer.Type.getByCode((Integer) attachmentData.get("type"));
        }


        @Override
        protected AbstractAttachment inst(ByteBuffer buffer, byte transactionVersion) {
            return new PocNodeType(buffer,transactionVersion);
        }

        @Override
        protected AbstractAttachment inst(JSONObject attachmentData) {
            return new PocNodeType(attachmentData);
        }

        @Override
        protected AbstractAttachment inst(int version) {
            return null;
        }


        @Override
        public int getMySize() {
            return 4 + 2 + ip.getBytes().length;
        }

        @Override
        public void putMyBytes(ByteBuffer buffer) {
            buffer.putInt(type.getCode());

            byte[] ip = Convert.toBytes(this.ip);
            buffer.putShort((short) ip.length);
            buffer.put(ip);
        }

        @Override
        public void putMyJSON(JSONObject attachment) {
            attachment.put("ip", this.ip);
            attachment.put("type", this.type.getCode());
        }

        @Override
        public TransactionType getTransactionType() {
          return PocTx.POC_NODE_TYPE;
        }
    }

    final class PocWeightTable extends Attachment.TxBodyBase {

        private Map<String, BigInteger> weightMap;
        private Map<Integer, BigInteger> nodeTypeTemplate;
        private Map<Integer, BigInteger> serverOpenTemplate;
        private Map<Integer, BigInteger> hardwareConfigTemplate;
        private Map<Integer, BigInteger> networkConfigTemplate;
        private Map<Integer, BigInteger> txHandlePerformanceTemplate;
        private Map<Integer, BigInteger> onlineRateOfficialTemplate;
        private Map<Integer, BigInteger> onlineRateCommunityTemplate;
        private Map<Integer, BigInteger> onlineRateHubBoxTemplate;
        private Map<Integer, BigInteger> onlineRateNormalTemplate;
        private Map<Integer, BigInteger> blockingMissTemplate;
        private Map<Integer, BigInteger> bocSpeedTemplate;
        
        private int templateVersion;

        public Map<String, BigInteger> getWeightMap() {
            return weightMap;
        }

        public Map<Integer, BigInteger> getNodeTypeTemplate() {
            return nodeTypeTemplate;
        }

        public Map<Integer, BigInteger> getServerOpenTemplate() {
            return serverOpenTemplate;
        }

        public Map<Integer, BigInteger> getHardwareConfigTemplate() {
            return hardwareConfigTemplate;
        }

        public Map<Integer, BigInteger> getNetworkConfigTemplate() {
            return networkConfigTemplate;
        }

        public Map<Integer, BigInteger> getTxHandlePerformanceTemplate() {
            return txHandlePerformanceTemplate;
        }

        public Map<Integer, BigInteger> getOnlineRateOfficialTemplate() {
            return onlineRateOfficialTemplate;
        }

        public Map<Integer, BigInteger> getOnlineRateCommunityTemplate() {
            return onlineRateCommunityTemplate;
        }

        public Map<Integer, BigInteger> getOnlineRateHubBoxTemplate() {
            return onlineRateHubBoxTemplate;
        }

        public Map<Integer, BigInteger> getOnlineRateNormalTemplate() {
            return onlineRateNormalTemplate;
        }

        public Map<Integer, BigInteger> getBlockingMissTemplate() {
            return blockingMissTemplate;
        }

        public Map<Integer, BigInteger> getBocSpeedTemplate() {
            return bocSpeedTemplate;
        }

        public int getTemplateVersion() {
            return templateVersion;
        }

        public void setTemplateVersion(int templateVersion) {
            this.templateVersion = templateVersion;
        }
        
        public static PocWeightTable defaultPocWeightTable(){
           Map<String, BigInteger> weightMap = new HashMap<>();
           weightMap.put("node", BigInteger.valueOf(25)); // 节点类型占比， 25%，先不算百分比
           weightMap.put("serverOpen", BigInteger.valueOf(20L)); // 开启服务占比，20%， 先不算百分比
           weightMap.put("ssHold", BigInteger.valueOf(40L)); // SS持有量占比， 40%，先不算百分比
           weightMap.put("hardwareConfig", BigInteger.valueOf(5L)); // 硬件配置占比，5%，先不算百分比
           weightMap.put("networkConfig", BigInteger.valueOf(5L)); // 网络配置占比， 5%，先不算百分比
           weightMap.put("txHandlePerformance", BigInteger.valueOf(5L)); //交易处理性能占比， 5%,先不算百分比
           
            Map<Integer, BigInteger> nodeTypeTemplate = new HashMap();
            nodeTypeTemplate.put(1, BigInteger.valueOf(10L)); // 基金会节点 -> key对应Peer.Type.OFFICIAL
            nodeTypeTemplate.put(2, BigInteger.valueOf(8L)); // 社区节点 -> key对应Peer.Type.COMMUNITY
            nodeTypeTemplate.put(4, BigInteger.valueOf(6L)); // HUB节点 -> key对应Peer.Type.HUB
            nodeTypeTemplate.put(5, BigInteger.valueOf(6L)); // BOX节点 -> key对应Peer.Type.BOX
            nodeTypeTemplate.put(3, BigInteger.valueOf(3L)); // 普通节点 -> key对应Peer.Type.NORMAL
            
            Map<Integer, BigInteger> serverOpenTemplate = new HashMap<>();
            serverOpenTemplate.put(1,BigInteger.valueOf(4L)); // 矿工服务开启 -> key对应Peer.Type.OFFICIAL
            serverOpenTemplate.put(2,BigInteger.valueOf(4L)); // 观察者服务开启 -> key对应Peer.Type.COMMUNITY
            serverOpenTemplate.put(4,BigInteger.valueOf(4L)); // 穿透者服务开启 -> key对应Peer.Type.HUB
            serverOpenTemplate.put(5,BigInteger.valueOf(4L)); // 存储者服务开启 -> key对应Peer.Type.BOX
            serverOpenTemplate.put(3,BigInteger.valueOf(4L)); // 证明者服务开启 -> key对应Peer.Type.NORMAL
            
            Map<Integer, BigInteger> hardwareConfigTemplate = new HashMap<>();
            hardwareConfigTemplate.put(1, BigInteger.valueOf(3L)); // 硬件配置低 -> key对应DeviceLevels.BAD.getLevel()
            hardwareConfigTemplate.put(2, BigInteger.valueOf(6L)); // 硬件配置中 -> key对应DeviceLevels.MIDDLE.getLevel()
            hardwareConfigTemplate.put(3, BigInteger.valueOf(10L)); // 硬件配置高 -> key对应DeviceLevels.GOOD.getLevel()
            
            Map<Integer, BigInteger> networkConfigTemplate = new HashMap<>();
            networkConfigTemplate.put(0, BigInteger.valueOf(0L)); // 网络配置极差 -> key对应DeviceLevels.POOR.getLevel()
            networkConfigTemplate.put(1, BigInteger.valueOf(3L)); // 网络配置差 -> key对应DeviceLevels.BAD.getLevel()
            networkConfigTemplate.put(2, BigInteger.valueOf(6L)); // 网络配置中 -> key对应DeviceLevels.MIDDLE.getLevel()
            networkConfigTemplate.put(3, BigInteger.valueOf(10L)); // 网络配置高 -> key对应DeviceLevels.GOOD.getLevel()
            
            Map<Integer, BigInteger> txHandlePerformanceTemplate = new HashMap<>();
            txHandlePerformanceTemplate.put(1, BigInteger.valueOf(3L)); // 交易处理性能低 -> key对应DeviceLevels.BAD.getLevel()
            txHandlePerformanceTemplate.put(2, BigInteger.valueOf(6L)); // 交易处理性能中 -> key对应DeviceLevels.MIDDLE.getLevel()
            txHandlePerformanceTemplate.put(3, BigInteger.valueOf(10L)); // 交易处理性能高 -> key对应DeviceLevels.GOOD.getLevel()
            
            Map<Integer, BigInteger> onlineRateOfficialTemplate = new HashMap<>();
            onlineRateOfficialTemplate.put(3, BigInteger.valueOf(-2L)); // 基金会节点在线率1  99%<在线率<99.99%
            onlineRateOfficialTemplate.put(2, BigInteger.valueOf(-5L)); // 基金会节点在线率2  97%<在线率<99%
            onlineRateOfficialTemplate.put(1, BigInteger.valueOf(-10L)); // 基金会节点在线率3  在线率<97%
            
            Map<Integer, BigInteger> onlineRateCommunityTemplate = new HashMap<>();
            onlineRateCommunityTemplate.put(3,BigInteger.valueOf(-2L)); // 社区节点在线率1  97%<在线率<99%
            onlineRateCommunityTemplate.put(2,BigInteger.valueOf(-5L)); // 社区节点在线率2  90%<在线率<97%
            onlineRateCommunityTemplate.put(1,BigInteger.valueOf(-10L)); // 社区节点在线率3  在线率<90%
            
            Map<Integer, BigInteger> onlineRateHubBoxTemplate = new HashMap<>();
            onlineRateHubBoxTemplate.put(3, BigInteger.valueOf(5L)); // HUB/BOX节点在线率1  99%<在线率
            onlineRateHubBoxTemplate.put(2, BigInteger.valueOf(3L)); // HUB/BOX节点在线率2  97%<在线率
            onlineRateHubBoxTemplate.put(1, BigInteger.valueOf(-5L)); // HUB/BOX节点在线率3  在线率<90%
            
            Map<Integer, BigInteger> onlineRateNormalTemplate = new HashMap<>();
            onlineRateNormalTemplate.put(3, BigInteger.valueOf(5L)); // 普通节点在线率1  97%<在线率
            onlineRateNormalTemplate.put(2, BigInteger.valueOf(3L)); // 普通节点在线率2  90%<在线率
            
            Map<Integer, BigInteger> blockingMissTemplate = new HashMap<>();
            blockingMissTemplate.put(1, BigInteger.valueOf(-10L)); // 丢失量高 -> key对应DeviceLevels.BAD.getLevel()
            blockingMissTemplate.put(2, BigInteger.valueOf(-6L)); // 丢失量中 -> key对应DeviceLevels.MIDDLE.getLevel()
            blockingMissTemplate.put(3, BigInteger.valueOf(-3L)); // 丢失量低 -> key对应DeviceLevels.GOOD.getLevel()
            
            Map<Integer, BigInteger> bocSpeedTemplate = new HashMap<>();
            bocSpeedTemplate.put(0, BigInteger.valueOf(-10L)); // 硬分叉 -> key对应DeviceLevels.POOR.getLevel()
            bocSpeedTemplate.put(1, BigInteger.valueOf(-6L)); // 分叉收敛慢 -> key对应DeviceLevels.BAD.getLevel()
            bocSpeedTemplate.put(2, BigInteger.valueOf(-3L)); // 分叉收敛中 -> key对应DeviceLevels.MIDDLE.getLevel()

            return new PocWeightTable(weightMap,nodeTypeTemplate,serverOpenTemplate,hardwareConfigTemplate,networkConfigTemplate,txHandlePerformanceTemplate,onlineRateOfficialTemplate,onlineRateCommunityTemplate,onlineRateHubBoxTemplate,onlineRateNormalTemplate,blockingMissTemplate,bocSpeedTemplate);
        }
        
        
        public PocWeightTable(Map<String, BigInteger> weightMap, Map<Integer, BigInteger> nodeTypeTemplate, Map<Integer, BigInteger> serverOpenTemplate, Map<Integer, BigInteger> hardwareConfigTemplate, Map<Integer, BigInteger> networkConfigTemplate, Map<Integer, BigInteger> txHandlePerformanceTemplate, Map<Integer, BigInteger> onlineRateOfficialTemplate, Map<Integer, BigInteger> onlineRateCommunityTemplate, Map<Integer, BigInteger> onlineRateHubBoxTemplate, Map<Integer, BigInteger> onlineRateNormalTemplate, Map<Integer, BigInteger> blockingMissTemplate, Map<Integer, BigInteger> bocSpeedTemplate) {
            this.weightMap = weightMap;
            this.nodeTypeTemplate = nodeTypeTemplate;
            this.serverOpenTemplate = serverOpenTemplate;
            this.hardwareConfigTemplate = hardwareConfigTemplate;
            this.networkConfigTemplate = networkConfigTemplate;
            this.txHandlePerformanceTemplate = txHandlePerformanceTemplate;
            this.onlineRateOfficialTemplate = onlineRateOfficialTemplate;
            this.onlineRateCommunityTemplate = onlineRateCommunityTemplate;
            this.onlineRateHubBoxTemplate = onlineRateHubBoxTemplate;
            this.onlineRateNormalTemplate = onlineRateNormalTemplate;
            this.blockingMissTemplate = blockingMissTemplate;
            this.bocSpeedTemplate = bocSpeedTemplate;
        }

        public PocWeightTable(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.weightMap = (Map<String, BigInteger>) _getByte(buffer);
            this.nodeTypeTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.serverOpenTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.hardwareConfigTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.networkConfigTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.txHandlePerformanceTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.onlineRateOfficialTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.onlineRateCommunityTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.onlineRateHubBoxTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.onlineRateNormalTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.blockingMissTemplate = (Map<Integer, BigInteger>) _getByte(buffer);
            this.bocSpeedTemplate =(Map<Integer, BigInteger>) _getByte(buffer);
        }

        public PocWeightTable(JSONObject attachmentData) {
            super(attachmentData);
            this.weightMap = (Map<String, BigInteger>) attachmentData.get("weight");
            this.nodeTypeTemplate = (Map<Integer, BigInteger>) attachmentData.get("node");
            this.serverOpenTemplate = (Map<Integer, BigInteger>) attachmentData.get("serverOpen");
            this.hardwareConfigTemplate = (Map<Integer, BigInteger>) attachmentData.get("hardwareConfig");
            this.networkConfigTemplate = (Map<Integer, BigInteger>) attachmentData.get("networkConfig");
            this.txHandlePerformanceTemplate = (Map<Integer, BigInteger>) attachmentData.get("txHandlePerformance");
            this.onlineRateOfficialTemplate =(Map<Integer, BigInteger>) attachmentData.get("onlineRateOfficial");
            this.onlineRateCommunityTemplate = (Map<Integer, BigInteger>) attachmentData.get("onlineRateCommunity");
            this.onlineRateHubBoxTemplate =(Map<Integer, BigInteger>) attachmentData.get("onlineRateHubBox");
            this.onlineRateNormalTemplate = (Map<Integer, BigInteger>) attachmentData.get("onlineRateNormal");
            this.blockingMissTemplate = (Map<Integer, BigInteger>) attachmentData.get("blockingMiss");
            this.bocSpeedTemplate = (Map<Integer, BigInteger>) attachmentData.get("bocSpeed");
        }

        @Override
        protected AbstractAttachment inst(ByteBuffer buffer, byte transactionVersion) {
            return new PocWeightTable(buffer,transactionVersion);
        }

        @Override
        protected AbstractAttachment inst(JSONObject attachmentData) {
            return new PocWeightTable(attachmentData);
        }

        @Override
        protected AbstractAttachment inst(int version) {
            return null;
        }

        @Override
        public int getMySize() {
            return _readByteSize(
                Lists.newArrayList(
                    weightMap,
                    nodeTypeTemplate,
                    serverOpenTemplate,
                    hardwareConfigTemplate,
                    networkConfigTemplate,
                    txHandlePerformanceTemplate,
                    onlineRateOfficialTemplate,
                    onlineRateCommunityTemplate,
                    onlineRateHubBoxTemplate,
                    onlineRateNormalTemplate,
                    blockingMissTemplate,
                    bocSpeedTemplate
                )
            );
        }

        @Override
        public void putMyBytes(ByteBuffer buffer) {
            _putByteSize(
                 buffer,
                 Lists.newArrayList(
                    weightMap,
                    nodeTypeTemplate,
                    serverOpenTemplate,
                    hardwareConfigTemplate,
                    networkConfigTemplate,
                    txHandlePerformanceTemplate,
                    onlineRateOfficialTemplate,
                    onlineRateCommunityTemplate,
                    onlineRateHubBoxTemplate,
                    onlineRateNormalTemplate,
                    blockingMissTemplate,
                    bocSpeedTemplate
                 )
            );
        }

        @Override
        public void putMyJSON(JSONObject attachment) {
            attachment.put("weight", weightMap);
            attachment.put("node", nodeTypeTemplate);
            attachment.put("serverOpen", serverOpenTemplate);
            attachment.put("hardwareConfig", hardwareConfigTemplate);
            attachment.put("networkConfig", networkConfigTemplate);
            attachment.put("txHandlePerformance", txHandlePerformanceTemplate);
            attachment.put("onlineRateOfficial", onlineRateOfficialTemplate);
            attachment.put("onlineRateCommunity", onlineRateCommunityTemplate);
            attachment.put("onlineRateHubBox", onlineRateHubBoxTemplate);
            attachment.put("onlineRateNormal", onlineRateNormalTemplate);
            attachment.put("blockingMiss", blockingMissTemplate);
            attachment.put("bocSpeed", bocSpeedTemplate);
        }

        @Override
        public TransactionType getTransactionType() {
          return PocTx.POC_WEIGHT_TABLE;
        }
    }

    final class PocNodeConf extends Attachment.TxBodyBase {

        private final String ip;
        private final String port;
        private SystemInfo systemInfo;

        public String getIp() {
            return ip;
        }

        public String getPort() {
            return port;
        }

        public SystemInfo getSystemInfo() {
            return systemInfo;
        }

        public PocNodeConf(String ip, String port, SystemInfo systemInfo) {
            this.ip = ip;
            this.port = port;
            this.systemInfo = systemInfo;
        }

        public PocNodeConf(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.ip = buffer.toString();
            this.port = buffer.toString();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
            try {
                ObjectInputStream ois = new ObjectInputStream(bais);
                Object obj = ois.readObject();
                if (obj instanceof SystemInfo) {
                    this.systemInfo = (SystemInfo) obj;
                } else {
                    this.systemInfo = null;
                }
                bais.close();
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public PocNodeConf(JSONObject attachmentData) {
            super(attachmentData);
            this.ip = (String) attachmentData.get("ip");
            this.port = (String) attachmentData.get("port");
            this.systemInfo = (SystemInfo) attachmentData.get("systemInfo");
        }

        @Override
        protected AbstractAttachment inst(ByteBuffer buffer, byte transactionVersion) {
            return new PocNodeConf(buffer,transactionVersion);
        }

        @Override
        protected AbstractAttachment inst(JSONObject attachmentData) {
            return new PocNodeConf(attachmentData);
        }

        @Override
        protected AbstractAttachment inst(int version) {
            return null;
        }

        @Override
        public int getMySize() {
            return ip.getBytes().length
              + port.getBytes().length
              + _readByteSize(systemInfo);
        }

        @Override
        public void putMyBytes(ByteBuffer buffer) {
            buffer.put(ip.getBytes());
            buffer.put(port.getBytes());

            _putByteSize(buffer, systemInfo);
        }

        @Override
        public void putMyJSON(JSONObject attachment) {
            attachment.put("ip", ip);
            attachment.put("port", port);
            attachment.put("systemInfo", systemInfo);
        }

        @Override
        public TransactionType getTransactionType() {
            return PocTx.POC_NODE_CONFIGURATION;
        }
    }

    final class PocOnlineRate extends Attachment.TxBodyBase {
        private final String ip;
        private final String port;
        private final int networkRate; // 网络在线率百分比的值乘以 100，用 int 表示, 例 99% = 9900， 99.99% = 9999

        public String getIp() {
            return ip;
        }

        public String getPort() {
            return port;
        }

        public int getNetworkRate() {
            return networkRate;
        }

        public PocOnlineRate(String ip, String port, int networkRate) {
            this.ip = ip;
            this.port = port;
            this.networkRate = networkRate;
        }

        public PocOnlineRate(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.ip = buffer.toString();
            this.port = buffer.toString();
            this.networkRate = buffer.getInt();
        }

        public PocOnlineRate(JSONObject attachmentData) {
            super(attachmentData);
            this.ip = (String) attachmentData.get("ip");
            this.port = (String) attachmentData.get("port");
            this.networkRate = (int) attachmentData.get("networkRate");
        }

        @Override
        protected AbstractAttachment inst(ByteBuffer buffer, byte transactionVersion) {
            return new PocOnlineRate(buffer,transactionVersion);
        }

        @Override
        protected AbstractAttachment inst(JSONObject attachmentData) {
            return new PocOnlineRate(attachmentData);
        }

        @Override
        protected AbstractAttachment inst(int version) {
            return null;
        }

        @Override
        public int getMySize() {
            return 2 + ip.getBytes().length + port.getBytes().length;
        }

        @Override
        public void putMyBytes(ByteBuffer buffer) {
            buffer.put(ip.getBytes());
            buffer.put(port.getBytes());
            buffer.putInt(networkRate);
        }

        @Override
        public void putMyJSON(JSONObject attachment) {
            attachment.put("ip", ip);
            attachment.put("port", port);
            attachment.put("networkRate", networkRate);
        }

        @Override
        public TransactionType getTransactionType() {
            return PocTx.POC_ONLINE_RATE;
        }
    }

    final class PocBlockMiss extends Attachment.TxBodyBase {
        private long missAccountId;
        private int blockMissTimeStamp;

        public PocBlockMiss(long missAccountId) {
            this.missAccountId = missAccountId;
        }

        public long getMissAccountId() {
            return missAccountId;
        }

        public int getBlockMissTimeStamp() {
            return blockMissTimeStamp;
        }

        public PocBlockMiss(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
             this.missAccountId = buffer.getLong();
             this.blockMissTimeStamp = buffer.getInt();
        }

        public PocBlockMiss(JSONObject attachmentData) {
            super(attachmentData);
            this.missAccountId = (long) attachmentData.get("missAccountId");
            this.blockMissTimeStamp = (int) attachmentData.get("blockMissTimeStamp");
        }

        @Override
        protected AbstractAttachment inst(ByteBuffer buffer, byte transactionVersion) {
            return new PocBlockMiss(buffer,transactionVersion);
        }

        @Override
        protected AbstractAttachment inst(JSONObject attachmentData) {
            return new PocBlockMiss(attachmentData);
        }

        @Override
        protected AbstractAttachment inst(int version) {
            return null;
        }

        @Override
        public int getMySize() {
            return 8 + 4;
        }

        @Override
        public void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(missAccountId);
            buffer.putInt(blockMissTimeStamp);
        }

        @Override
        public void putMyJSON(JSONObject json) {
              json.put("missAccountId", missAccountId);
              json.put("blockMissTimeStamp", blockMissTimeStamp);
        }

        @Override
        public TransactionType getTransactionType() {
            return PocTx.POC_BLOCKING_MISS;
        }
    }

    /**
     * Bifurcation of convergence for PoC
     */
    final class PocBC extends Attachment.TxBodyBase {
        private final String ip;
        private final String port;
        private final int speed; // 分叉收敛速度 1-硬分叉；2-慢；3-中；4-快

        public String getIp() {
            return ip;
        }

        public String getPort() {
            return port;
        }

        public int getSpeed() {
            return speed;
        }

        public PocBC(String ip, String port, int speed) {
            this.ip = ip;
            this.port = port;
            this.speed = speed;
        }

        public PocBC(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.ip = buffer.toString();
            this.port = buffer.toString();
            this.speed = buffer.getInt();
        }

        public PocBC(JSONObject attachmentData) {
            super(attachmentData);
            this.ip = (String) attachmentData.get("ip");
            this.port = (String) attachmentData.get("port");
            this.speed = (int) attachmentData.get("speed");
        }

        @Override
        protected AbstractAttachment inst(ByteBuffer buffer, byte transactionVersion) {
            return new PocBC(buffer,transactionVersion);
        }

        @Override
        protected AbstractAttachment inst(JSONObject attachmentData) {
            return new PocBC(attachmentData);
        }

        @Override
        protected AbstractAttachment inst(int version) {
            return null;
        }

        @Override
        public int getMySize() {
            return 2 + ip.getBytes().length + port.getBytes().length;
        }

        @Override
        public void putMyBytes(ByteBuffer buffer) {
            buffer.put(ip.getBytes());
            buffer.put(port.getBytes());
            buffer.putInt(speed);
        }

        @Override
        public void putMyJSON(JSONObject json) {
            json.put("ip", ip);
            json.put("port", port);
            json.put("speed", speed);
        }

        @Override
        public TransactionType getTransactionType() {
            return PocTx.POC_BC;
        }
    }
}
