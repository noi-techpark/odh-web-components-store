package it.bz.opendatahub.webcomponents.crawlerservice.repository;

import it.bz.opendatahub.webcomponents.crawlerservice.data.struct.CommitEntry;
import it.bz.opendatahub.webcomponents.crawlerservice.data.struct.GitRemote;
import it.bz.opendatahub.webcomponents.crawlerservice.data.struct.TagEntry;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface VcsApiRepository {
    List<TagEntry> listVersionTags(GitRemote gitRemote);

    CommitEntry getMetadataForCommit(GitRemote gitRemote, String ref);

    String getLatestRevisionHash(GitRemote gitRemote);

    String getLatestRevisionHash(GitRemote gitRemote, String branchName);

    ByteArrayOutputStream getFileContents(GitRemote gitRemote, String revisionHash, String remotePathToFile);
}