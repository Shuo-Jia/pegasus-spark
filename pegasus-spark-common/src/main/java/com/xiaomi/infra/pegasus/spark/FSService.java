package com.xiaomi.infra.pegasus.spark;

import com.xiaomi.infra.galaxy.fds.client.FDSClientConfiguration;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDS;
import com.xiaomi.infra.galaxy.fds.client.GalaxyFDSClient;
import com.xiaomi.infra.galaxy.fds.client.credential.BasicFDSCredential;
import com.xiaomi.infra.galaxy.fds.client.exception.GalaxyFDSClientException;
import com.xiaomi.infra.galaxy.fds.client.model.FDSObject;
import com.xiaomi.infra.galaxy.fds.model.FDSObjectMetadata;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class FSService implements Serializable {

  private static final Log LOG = LogFactory.getLog(FSService.class);
  public FSConfig fsConfig;

  public FSService() {}

  public FSService(FSConfig fsConfig) {
    this.fsConfig = fsConfig;
  }

  public BufferedReader getReader(String filePath) throws PegasusSparkException {
    try {
      InputStream inputStream =
          FileSystem.get(new URI(filePath), new Configuration()).open(new Path(filePath));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      return bufferedReader;
    } catch (Exception e) {
      LOG.error("get filePath reader failed from " + filePath, e);
      throw new PegasusSparkException("get filePath reader failed, [url: " + filePath + "]" + e);
    }
  }

  public BufferedWriter getWriter(String filePath)
      throws URISyntaxException, PegasusSparkException {
    try {
      OutputStreamWriter outputStreamWriter =
          new OutputStreamWriter(
              FileSystem.get(new URI(filePath), new Configuration()).create(new Path(filePath)));
      BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
      return bufferedWriter;
    } catch (Exception e) {
      LOG.error("get filePath writer failed from " + filePath, e);
      throw new PegasusSparkException("get filePath writer failed, [url: " + filePath + "]" + e);
    }
  }

  public FileStatus[] getFileStatus(String path) throws IOException {
    FileSystem fs = FileSystem.get(URI.create(path), new Configuration());
    Path Path = new Path(path);
    return fs.listStatus(Path);
  }

  public String getFileMD5(String filePath)
      throws GalaxyFDSClientException, IOException, URISyntaxException {
    // if user target system is fds, here choose fds api get md5 directly instead of calculate,
    // which consuming few time
    if (filePath.contains("fds")) {
      return getMD5(filePath);
    } else {
      return calculateMD5(filePath);
    }
  }

  private String getMD5(String filePath)
      throws GalaxyFDSClientException, IOException, URISyntaxException {
    if (fsConfig != null && fsConfig.remoteFsEndPoint != null) {
      FDSClientConfiguration fdsClientConfiguration =
          new FDSClientConfiguration(fsConfig.remoteFsEndPoint);
      fdsClientConfiguration.enableCdnForDownload(false);
      fdsClientConfiguration.enableCdnForUpload(false);

      GalaxyFDS fdsClient =
          new GalaxyFDSClient(
              new BasicFDSCredential(fsConfig.remoteFsAccessKey, fsConfig.remoteFsAccessSecret),
              fdsClientConfiguration);
      FDSObject fdsObject =
          fdsClient.getObject(
              fsConfig.remoteFsBucketName, filePath.split(fsConfig.remoteFsEndPoint + "/")[1]);
      FDSObjectMetadata metaData = fdsObject.getObjectMetadata();
      return metaData.getContentMD5();
    } else {
      LOG.warn("you now use calculateMD5 get md5 value!");
      return calculateMD5(filePath);
    }
  }

  public String calculateMD5(String filePath) throws IOException, URISyntaxException {
    return DigestUtils.md5Hex(
        FileSystem.get(new URI(filePath), new Configuration()).open(new Path(filePath)));
  }
}
