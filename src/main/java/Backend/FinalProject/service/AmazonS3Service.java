package Backend.FinalProject.service;

import Backend.FinalProject.domain.ImageFile;
import Backend.FinalProject.dto.ResponseDto;
import Backend.FinalProject.repository.FilesRepository;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AmazonS3Service {

    private final AmazonS3Client amazonS3Client;
    private final FilesRepository filesRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    //파일업로드
    @Transactional
    public ResponseDto<?> uploadFile(MultipartFile multipartFile, String filePath) {
        if (validateFileExists(multipartFile))      // 빈 파일인지 확인
            return ResponseDto.fail("NO-IMAGE-FILE","등록된 이미지가 없습니다.");
        String contentType = multipartFile.getContentType();
        assert contentType != null;
        if (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/bmp"))
            return ResponseDto.fail("IMAGE CONTENT-TYPE", "JPG, PNG, BMP 만 업로드 가능합니다.");

        String fileName = createFileName(multipartFile.getOriginalFilename());  // 난수파일이름생성 (난수이름+파일이름)
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());          // ObjectMetadata 에 파일 타입, byte 크기 넣어주기. 넣지않으면 IDE 상에서 설정하라는 권장로그가 뜸
        objectMetadata.setContentLength(multipartFile.getSize());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName + filePath, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));        // S3에 업로드
        } catch (IOException e) {
            log.info("AmazonS3Service uploadFile UPLOAD-FAILED");
            return ResponseDto.fail("UPLOAD-FAILED","파일 업로드 실패");
        }
        ImageFile imageMapper = ImageFile.builder()                         // 업로드한 파일들을 관리할 테이블에 파일이름, URL 넣기
                .url(amazonS3Client.getUrl(bucketName+filePath, fileName).toString())
                .imageName(fileName)
                .build();
        filesRepository.save(imageMapper);
        return ResponseDto.success(imageMapper);
    }

    //유니크한 파일이름 생성
    private String createFileName(String originalFileName) {
        return UUID.randomUUID().toString().concat(originalFileName);
    }

    //파일삭제
    @Transactional
    public boolean removeFile(String fileName, String filePath) {
        Optional<ImageFile> optionalImageMapper = filesRepository.findByImageName(fileName); // 파일이름으로 파일가져오기
        if (optionalImageMapper.isEmpty())    // 실제있는 파일인지 확인
            return true;
        ImageFile image = optionalImageMapper.get();
        filesRepository.deleteById(image.getId());    // imageMapper 에서 삭제
        DeleteObjectRequest request = new DeleteObjectRequest(bucketName + filePath, fileName); // 삭제 request 생성
        amazonS3Client.deleteObject(request);      // s3에서 파일삭제
        return false;
    }

    //실제있는 파일인지 확인
    private boolean validateFileExists(MultipartFile multipartFile) {
        return multipartFile.isEmpty();
    }
}