## 组织结构说明



目录结构如下：

```
├─conf
│  └─deserializers
├─controllers
├─hash
├─model
├─network
│  └─replicator
├─service
│  ├─dto
│  └─tipselection
│      └─impl
├─storage
│  └─rocksDB
├─utils
│  └─collections
│      ├─impl
│      └─interfaces
└─zmq
```

1. `conf`：项目中各类配置架构，包括**API**，**节点**，**网络**，**协议**等；
2. `controllers`：包括**地址**，**标识**，**HASH**，**事务**，**共识**等各类数据流的控制流程；
3. `hash`：主要包括各种**加密**、**算法**等信息；
4. `model`：定义各类数据模型；
5. `network`：处理节点间的网络通信；
6. `service`：包装了协议的核心内容，如**TIP选择**，**交易验证**等；
7. `storage`：存储单元；
8. `utils`：工具包；
9. `zmq`：一个基于内存的消息平台；