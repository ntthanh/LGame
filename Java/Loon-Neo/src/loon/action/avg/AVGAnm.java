/**
 * Copyright 2008 - 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.3.3
 */
package loon.action.avg;

import loon.BaseIO;
import loon.LRelease;
import loon.LSystem;
import loon.LTexture;
import loon.action.avg.drama.Expression;
import loon.canvas.LColor;
import loon.geom.PointI;
import loon.utils.MathUtils;
import loon.utils.StringUtils;
import loon.utils.TArray;
import loon.utils.parse.StrTokenizer;

//0.3.3新增类,用以按指定的格式循环播放动画图像
public class AVGAnm implements Expression, LRelease {

	protected final PointI point = new PointI();

	private String path;

	protected float alpha = 1.0f;

	protected float angle;

	protected int width, height, imageWidth, imageHeight;

	protected TArray<Integer> posxTmps = new TArray<Integer>();

	protected TArray<Integer> posyTmps = new TArray<Integer>();

	protected int[] posx = null;

	protected int[] posy = null;

	protected TArray<Integer> time = new TArray<Integer>();

	protected int tmp_time = 20;

	protected int alltime = 0;

	protected int count = 0;

	protected long startTime = -1;

	protected boolean loop = true, load = false, closed = false;;

	protected LTexture texture;

	protected LColor color;

	public AVGAnm(String resName) {
		open(BaseIO.loadText(resName));
	}

	public AVGAnm() {

	}

	public void open(String text) {
		try {
			StrTokenizer reader = new StrTokenizer(text, LSystem.NL);
			String script = null;
			for (; reader.hasMoreTokens();) {
				script = reader.nextToken().trim();
				if (script.length() > 0 && !script.startsWith(FLAG_L_TAG) && !script.startsWith(FLAG_C_TAG)
						&& !script.startsWith(FLAG_I_TAG)) {
					final String[] element = script.split(";");
					for (int j = 0; j < element.length; j++) {
						load(element[j]);
					}
				}
			}
		} catch (Throwable ex) {
			this.load = false;
			LSystem.error("AVGAnm exception", ex);
			return;
		}
		this.load = true;
		this.count = posxTmps.size;
		this.posx = new int[count];
		this.posy = new int[count];
		for (int i = 0; i < count; i++) {
			this.posx[i] = (posxTmps.get(i));
			this.posy[i] = (posyTmps.get(i));
		}
		if (width == 0) {
			width = imageWidth;
		}
		if (height == 0) {
			height = imageHeight;
		}
	}

	private void load(String script) {
		String[] op = script.split("=");
		if (op.length == 2) {
			final String key = op[0].trim();
			final String value = op[1].trim();
			if ("path".equalsIgnoreCase(key)) {
				path = StringUtils.replace(value, "\"", LSystem.EMPTY);
				if (texture != null) {
					texture.close();
					texture = null;
				}
				texture = LSystem.loadTexture(path);
				imageWidth = texture.getWidth();
				imageHeight = texture.getHeight();

			}
			if ("imagewidth".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value)) {
					imageWidth = Integer.parseInt(value);
				}
			} else if ("alpha".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value)) {
					alpha = Float.parseFloat(value);
				}
			} else if ("angle".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value)) {
					angle = Float.parseFloat(value);
				}
			} else if ("color".equalsIgnoreCase(key)) {
				String[] p = value.split(",");
				if (p.length > 2 && p.length < 5) {
					for (int i = 0; i < p.length; i++) {
						p[i] = p[i].replaceAll("^[\\t ]*", "").replaceAll("[\\t ]*$", LSystem.EMPTY);
					}
					if (p.length == 3) {
						color = new LColor(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
					}
					if (p.length == 4) {
						color = new LColor(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]),
								Integer.parseInt(p[3]));
					}
				}
			} else if ("imageheight".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value))
					imageHeight = Integer.parseInt(value);
			} else if ("width".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value)) {
					width = Integer.parseInt(value);
				}
			} else if ("height".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value)) {
					height = Integer.parseInt(value);
				}
			} else if ("time".equalsIgnoreCase(key)) {
				if (MathUtils.isNan(value)) {
					tmp_time = Integer.parseInt(value);
				}
			} else if ("pos".equalsIgnoreCase(key)) {
				String[] p = value.split(",");
				for (int i = 0; i < p.length; i++) {
					p[i] = p[i].replaceAll("^[\\t ]*", "").replaceAll("[\\t ]*$", LSystem.EMPTY);
				}
				switch (p.length) {
				case 1:
					if (MathUtils.isNan(p[0])) {
						posxTmps.add(Integer.parseInt(p[0]));
						posyTmps.add(Integer.parseInt(p[0]));
						time.add(tmp_time);
						alltime += tmp_time;
					}
					break;
				case 2:
					if (MathUtils.isNan(p[0]) && MathUtils.isNan(p[1])) {
						posxTmps.add(Integer.parseInt(p[0]));
						posyTmps.add(Integer.parseInt(p[1]));
						time.add(tmp_time);
						alltime += tmp_time;
					}
					break;
				}
			}
		}
	}

	public String getPath() {
		return path;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(int imageWidth) {
		this.imageWidth = imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(int imageHeight) {
		this.imageHeight = imageHeight;
	}

	public int[] getPosx() {
		return posx;
	}

	public int[] getPosy() {
		return posy;
	}

	public TArray<Integer> getTime() {
		return time;
	}

	public int getAlltime() {
		return alltime;
	}

	public int getCount() {
		return count;
	}

	public boolean isLoop() {
		return loop;
	}

	public final LTexture getTexture() {
		return texture;
	}

	public float getAlpha() {
		return alpha;
	}

	public float getAngle() {
		return angle;
	}

	public void start(long elapsedTime, boolean loop) {
		this.startTime = elapsedTime;
		this.loop = loop;
		if (texture != null) {
			if (texture.getWidth() == imageWidth && texture.getHeight() == imageHeight) {
				this.loop = false;
			}
		}
	}

	public void start() {
		this.start(0, loop);
	}

	public void stop() {
		this.startTime = -1;
		this.loop = false;
	}

	public PointI getPos(long elapsedTime) {
		if (startTime != -1) {
			int frame = getFrame(elapsedTime);
			point.x = posx[frame];
			point.y = posy[frame];
		}
		return point;
	}

	private int getFrame(long elapsedTime) {
		long diff = elapsedTime - startTime;
		if (!loop && diff >= alltime) {
			startTime = -1;
			return 0;
		}
		long now = diff % alltime;
		int t = 0;
		for (int i = 0; i < count; i++) {
			t += time.get(i);
			if (now < t) {
				return i;
			}
		}
		return 0;
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() {
		stop();
		if (texture != null) {
			texture.close();
			texture = null;
		}
		closed = true;
	}

}
