package com.xiaomi.infra.pegasus.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rocksdb.*;

/**
 * The wrapper of RocksDB Options in JNI.
 *
 * <p>NOTE: Must be closed manually to release the underlying memory.
 */
public class RocksDBOptions {

  private static final Log LOG = LogFactory.getLog(RocksDBOptions.class);

  public Options options = new Options();
  public ReadOptions readOptions = new ReadOptions();
  private Env env;

  public RocksDBOptions(String remoteFsUrl, String remoteFsPort) throws FDSException {
    if (remoteFsUrl.startsWith("fds://")) {
      env = new HdfsEnv(remoteFsUrl + "#" + remoteFsPort);
    } else if (remoteFsUrl.startsWith("hdfs://")) {
      env = new HdfsEnv(remoteFsUrl + ":" + remoteFsPort);
    } else {
      throw new FDSException("the url is illegal!");
    }

    Logger rocksDBLog =
        new Logger(options) {
          @Override
          public void log(InfoLogLevel infoLogLevel, String s) {
            LOG.info("[rocksDB native log info]" + s);
          }
        };
    options.setCreateIfMissing(true).setEnv(env).setLogger(rocksDBLog).setMaxOpenFiles(200);
    readOptions.setReadaheadSize(1024);
  }

  public void close() {
    options.close();
    readOptions.close();
    env.close();
  }
}
