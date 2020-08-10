package common.system.files;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Queue;

import common.pack.Source;
import common.pack.Context.ErrType;
import common.system.fake.FakeImage;

public interface FileData {

	public static Queue<String> IS2L(InputStream is) {
		try {
			Queue<String> ans = new ArrayDeque<>();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader reader = new BufferedReader(isr);
			String temp = null;
			while ((temp = reader.readLine()) != null)
				ans.add(temp);
			reader.close();
			return ans;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public default byte[] getBytes() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = getStream();
			int data;
			while ((data = is.read()) >= 0)
				baos.write(data);
			is.close();
			return baos.toByteArray();
		} catch (Exception e) {
			Source.ctx.noticeErr(e, ErrType.FATAL, "failed to read data");
			return null;
		}
	}

	public FakeImage getImg();

	public InputStream getStream() throws Exception;

	public Queue<String> readLine();

}

interface ByteData extends FileData {

	@Override
	public byte[] getBytes();

	@Override
	public default FakeImage getImg() {
		try {
			return FakeImage.read(getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public default InputStream getStream() {
		return new ByteArrayInputStream(getBytes());
	}

	@Override
	public default Queue<String> readLine() {
		return FileData.IS2L(new ByteArrayInputStream(getBytes()));
	}

}
