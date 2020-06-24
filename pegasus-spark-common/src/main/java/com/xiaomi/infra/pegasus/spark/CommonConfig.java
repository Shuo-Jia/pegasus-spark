package com.xiaomi.infra.pegasus.spark;

import java.io.Serializable;

public class CommonConfig implements Serializable {

  public String remoteFileSystemURL;
  public String remoteFileSystemPort;
  public RemoteFileSystem remoteFileSystem;
  public String clusterName;
  public String tableName;

  public CommonConfig(HDFSConfig hdfsConfig, String clusterName, String tableName) {
    initConfig(hdfsConfig, clusterName, tableName);
    this.remoteFileSystem = new HDFSFileSystem();
  }

  public CommonConfig(FDSConfig fdsConfig, String clusterName, String tableName) {
    initConfig(fdsConfig, clusterName, tableName);
    this.remoteFileSystem = new FDSFileSystem(fdsConfig);
  }

  private void initConfig(HDFSConfig config, String clusterName, String tableName) {
    this.remoteFileSystemURL = config.remoteFsUrl;
    this.remoteFileSystemPort = config.remoteFsPort;
    this.clusterName = clusterName;
    this.tableName = tableName;
  }
}