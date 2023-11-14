package jewellery.inventory.helper.mocks;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CorrectImageMockData implements MultipartFile {
    @Override
    public String getName() {
        return "image";
    }

    @Override
    public String getOriginalFilename() {
        return "image.jpg";
    }

    @Override
    public String getContentType() {
        return "image/png";
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getSize() {
        return 5;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return new byte[0];
    }

    @Override
    public InputStream getInputStream() throws IOException {
    return new FileInputStream("src/test/resources/static/img/pearl.jpg");
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {

    }
}
