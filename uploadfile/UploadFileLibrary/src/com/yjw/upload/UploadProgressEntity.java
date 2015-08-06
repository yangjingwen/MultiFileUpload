package com.yjw.upload;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * ��дHttpEntityWrapper����ϴ����ȡ�HttpEntityWrapper��HttpEntity�ӿڵ�����
 * Date:2015.08.05
 */
public class UploadProgressEntity extends HttpEntityWrapper {
	/**���ȼ�������**/
    private final ProgressListener listener;
    public UploadProgressEntity(final HttpEntity entity,final ProgressListener listener) {
        super(entity);
        this.listener = listener;
    }

    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;

        CountingOutputStream(final OutputStream out,
                final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        @Override
        public void write(final byte[] b, final int off, final int len)
                throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        @Override
        public void write(final int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }

    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        this.wrappedEntity.writeTo(out instanceof CountingOutputStream ? out
                : new CountingOutputStream(out, this.listener));
    }
    /**
     * ���ȼ������ӿ�
     */
    public interface ProgressListener {
        public void transferred(long transferedBytes);
    }
}