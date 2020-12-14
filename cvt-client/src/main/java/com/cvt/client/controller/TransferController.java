package com.cvt.client.controller;

import com.cvt.client.config.UploadConfig;
import com.cvt.client.util.FileUtils;
import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jota.CvtAPI;
import jota.dto.response.GetBalancesAndFormatResponse;
import jota.dto.response.GetNewAddressResponse;
import jota.dto.response.SendTransferResponse;
import jota.model.Transfer;
import jota.utils.SeedRandomGenerator;
import jota.utils.TrytesConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.cvt.client.core.CvtConfig.*;

/**
 * AddressController
 *
 * @author cvt admin
 *
 */
@Slf4j
@Api(value = "通用操作")
@RestController
@RequestMapping("/")
public class TransferController {

    @Autowired
    protected CvtAPI cvtAPI;

    @Autowired
    protected UploadConfig uploadConfig;

    @ApiOperation("生成一个新的种子")
    @GetMapping("/new-seed")
    public String newSeed() {
        return SeedRandomGenerator.generateNewSeed();
    }

    @ApiOperation("生成一个新的地址")
    @GetMapping("/new-address")
    public String newAddress(@ApiParam("种子") @RequestParam String seed) throws Exception {
        GetNewAddressResponse addressResponse = cvtAPI.generateNewAddresses(seed, SECURITY_LEVEL, false, 1);
        return addressResponse.first();
    }

    @ApiOperation("存储文件")
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@ApiParam("存储文件") @RequestParam("file") MultipartFile file) throws Exception {
        if (null == file) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String dirPath = uploadConfig.getSharedUploadPath();
        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }
        dirPath += cvtAPI.getHost() + "_" + cvtAPI.getPort() + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String rndFileName = getRandomString(20);
        if (new File(rndFileName).exists()) {
            throw new RuntimeException("文件已经存在");
        }
        String fileOutDir = dirPath + rndFileName;
        File tmpFile = File.createTempFile(rndFileName, null);
        try (OutputStream os = new FileOutputStream(tmpFile)) {
            IOUtils.copy(file.getInputStream(), os);
        }

        splitFile(tmpFile.getAbsolutePath(), fileOutDir);

        List<FileUtils.FileHashPair> fileHashPairList = FileUtils.signFiles(fileOutDir);
        log.info("File Hashs: {}", new Gson().toJson(fileHashPairList));
        File indexDir = new File(uploadConfig.getIndexPath());
        if (!indexDir.exists()) {
            indexDir.mkdirs();
        }
        try(FileOutputStream os = new FileOutputStream(new File(uploadConfig.getIndexPath() + File.separator + rndFileName + ".index"))) {
            IOUtils.write(new Gson().toJson(fileHashPairList), os, StandardCharsets.UTF_8);
        }
        return ResponseEntity.ok(cvtAPI.getHost() + "_" + cvtAPI.getPort() + "/" + rndFileName);
    }

    @ApiOperation("查询余额")
    @PostMapping("/balance")
    public Long getBalance(@ApiParam("种子") @RequestParam String seed) throws Exception {
        GetBalancesAndFormatResponse response = cvtAPI.getInputs(seed, SECURITY_LEVEL, 0, 0, 0);
        return response.getTotalBalance();
    }

    @ApiOperation("转账")
    @PostMapping("/transfer")
    public Boolean newAddress(@ApiParam("种子") @RequestParam String seed,
                           @ApiParam("目标地址") @RequestParam String addressTo,
                           @ApiParam("转账数量") @RequestParam Integer amount,
                           @ApiParam("存储文件路径") @RequestParam("file") String filePath) throws Exception {

        GetNewAddressResponse remaindAddressResponse = cvtAPI.generateNewAddresses(seed, SECURITY_LEVEL, false, 1);
        String remainderAddress = remaindAddressResponse.getAddresses().get(0);

        String testTag = "CVTJAVASPAM999999999999999";
        Transfer transfer = new Transfer(addressTo, amount, TrytesConverter.asciiToTrytes(filePath), testTag);
        SendTransferResponse response = doTransfer(seed, Collections.singletonList(transfer), remainderAddress);
        if (null == response.getSuccessfully() || response.getSuccessfully().length == 0) {
            throw new RuntimeException("转账失败");
        }
        for (Boolean result : response.getSuccessfully()) {
            if (!result) {
                throw new RuntimeException("转账失败");
            }
        }
        return true;
    }

    private void splitFile(String src, String outDir) throws Exception {
        log.info("Split file data to dir: {}", outDir);
        FileUtils.split(src, outDir, 1024 * 1024);
    }

    /**
     * 随机获取字符串
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String getRandomString(int length) {
        if (length <= 0) {
            return "";
        }
        char[] randomChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd',
                'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm'};
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(randomChar[Math.abs(random.nextInt()) % randomChar.length]);
        }
        return stringBuilder.toString();
    }

    protected SendTransferResponse doTransfer(String seed, List<Transfer> transfer, String addressRemainder) throws Exception {
        return cvtAPI.sendTransfer(seed, SECURITY_LEVEL, DEPTH, MIN_WEIGHT,
                transfer, null, addressRemainder, false, false, null);
    }
}
