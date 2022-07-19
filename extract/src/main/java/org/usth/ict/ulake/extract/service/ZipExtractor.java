package org.usth.ict.ulake.extract.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usth.ict.ulake.common.model.dashboard.FileFormModel;
import org.usth.ict.ulake.common.model.folder.FileModel;
import org.usth.ict.ulake.common.model.folder.FolderModel;
import org.usth.ict.ulake.common.service.exception.LakeServiceException;
import org.usth.ict.ulake.extract.model.ExtractRequest;
import org.usth.ict.ulake.extract.model.ExtractResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class ZipExtractor extends Extractor {
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ExtractTask.class);

    public ZipExtractor() {
        super();
    }

    @Override
    public void extract(String bearer, ExtractRequest request, ExtractResult result, ExtractCallback callback) {
        FileModel fileModel = null;
        FolderModel parent = null;
        try {
            log.info("Before getting file info {}", request.fileId, bearer);
            var info = dashboardService.fileInfo(request.fileId, bearer);
            log.info("after getting file info, resp {}", info.getResp().toString());
            fileModel = mapper.convertValue(info.getResp(), FileModel.class);
        }
        catch (LakeServiceException e) {
            result.progress = -1L;  // indicates an error
            e.printStackTrace();
            return;
        }

        try {
            var info = dashboardService.folderInfo(bearer, request.folderId);
            parent = mapper.convertValue(info.getResp(), FolderModel.class);
        }
        catch (LakeServiceException e) {
            result.progress = -2L;  // indicates an error
            return;
        }

        log.info("   + ZIP file cid {}, name {}", fileModel.cid, fileModel.name);
        log.info("   + Preparing to fetch file id {} from core", fileModel.id);
        InputStream fis = coreService.objectDataByFileId(fileModel.id, bearer);
        ZipInputStream zis = new ZipInputStream(fis);
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                log.info("Zip dir {}, entry {}", entry.isDirectory(), entry.getName());
                save(bearer, zis, entry, parent);
                entry = zis.getNextEntry();
             }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            log.error("   + Error reading zip entry {}", e.getMessage());
            result.requestId = -1L;  // indicates an error
        }
    }

    /**
     * Extract a zip entry and push to a target folder using dashboard service
     * @param entry
     * @param folderId  0 or null to get to user's root
     * @return
     * @throws IOException
     */
    private Object save(String bearer, ZipInputStream zis, ZipEntry entry, FolderModel parent) throws IOException {
        // TODO: nested directory support
        if (entry.isDirectory()) {
            // make a new dir
            FolderModel folder = new FolderModel();
            folder.name = entry.getName();
            folder.parent = parent;
            var resp = dashboardService.newFolder(bearer, folder).getResp();
            return mapper.convertValue(resp, FolderModel.class);
        }
        else {
            FileModel file = new FileModel();
            file.name = entry.getName();
            file.parent = parent;
            file.size = entry.getSize();

            FileFormModel fileModel = new FileFormModel();
            fileModel.fileInfo = file;
            fileModel.is = zis;
            var resp = dashboardService.newFile(bearer, fileModel);
            return mapper.convertValue(resp, FolderModel.class);
        }
    }
}
