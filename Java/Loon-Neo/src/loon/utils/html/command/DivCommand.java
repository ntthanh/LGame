/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
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
 * @version 0.5
 */
package loon.utils.html.command;

import loon.LSystem;
import loon.canvas.LColor;
import loon.component.Print;
import loon.component.layout.HorizontalAlign;
import loon.font.FontUtils;
import loon.font.IFont;
import loon.font.LFont;
import loon.geom.PointF;
import loon.opengl.GLEx;
import loon.utils.StringUtils;
import loon.utils.html.HtmlElement;
import loon.utils.html.css.CssStyleSheet;

public class DivCommand extends DisplayCommand {

	protected String text;

	protected LColor color;

	protected IFont font;

	private HorizontalAlign align;

	private boolean dirty;

	private HtmlElement node;

	private int sysSize;

	private float fontSize;

	private String fontName;

	public DivCommand(CssStyleSheet sheet, float width, float height, String fontName, float fontSize, LColor color) {
		super(sheet, "Div", width, height, color);
		this.fontName = fontName;
		this.fontSize = fontSize;
	}

	@Override
	public void parser(HtmlElement e) {
		super.parser(e);

		String sysFont = StringUtils.isEmpty(fontName) ? LSystem.getSystemGameFontName().toLowerCase() : fontName;

		String newFontName = null;
		this.node = e;

		if (bodyElement != null) {
			newFontName = bodyElement.getFontName() == null ? bodyElement.getFontName() : sysFont;
			color = bodyElement.getFontColor();
			fontSize = bodyElement.getFontSize();
		}
		if (selfElement != null) {
			newFontName = selfElement.getFontName() == null ? selfElement.getFontName() : sysFont;
			color = selfElement.getFontColor();
			fontSize = selfElement.getFontSize();
		}
		if (attrElement != null) {
			newFontName = attrElement.getFontName() == null ? attrElement.getFontName() : sysFont;
			color = attrElement.getFontColor();
			fontSize = attrElement.getFontSize();
		}

		sysSize = LSystem.getSystemGameFont().getSize();
		if (fontSize <= 1) {
			fontSize = sysSize;
		}

		if (newFontName != null && !newFontName.equals(sysFont)) {
			font = LFont.getFont(newFontName, (int) fontSize);
		} else {
			font = LSystem.getSystemGameFont();
		}

		text = e.getData();
		color = defaultColor;

		String alignName = e.getAttribute("align", null);
		if (!StringUtils.isEmpty(alignName)) {
			alignName = alignName.trim().toLowerCase();
		}

		if ("right".equals(alignName)) {
			align = HorizontalAlign.RIGHT;
		} else if ("center".equals(alignName)) {
			align = HorizontalAlign.CENTER;
		} else {
			align = HorizontalAlign.LEFT;
		}

		dirty = true;
	}

	@Override
	public void update() {

		if (dirty && text != null) {
			text = Print.prepareString(text, font, screenWidth - rect.x - rect.left - rect.right);
			PointF fontSize = FontUtils.getTextWidthAndHeight(font, text);
			rect.width = fontSize.x;
			rect.height = fontSize.y;
			dirty = false;
		}

		if (node != null && node.isP()) {
			rect.y += sysSize;
			rect.height += sysSize;
		}
	}

	@Override
	public void paint(GLEx g, float x, float y) {
		IFont temp = g.getFont();
		g.setFont(font);

		switch (align) {
		case RIGHT:
			g.drawString(text, screenWidth - rect.width + x + rect.left, rect.y + y + rect.top, color);
			break;
		case CENTER:
			g.drawString(text, (screenWidth - rect.width) / 2 + x + rect.left, rect.y + y + rect.top, color);
			break;
		default:
			g.drawString(text, rect.x + x + rect.left, rect.y + y + rect.top, color);
			break;
		}
		g.setFont(temp);
	}

	@Override
	public void close() {

	}

}
