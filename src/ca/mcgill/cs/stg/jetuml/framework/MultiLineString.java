/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015 Cay S. Horstmann and the contributors of the 
 * JetUML project.
 *
 * See: https://github.com/prmr/JetUML
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package ca.mcgill.cs.stg.jetuml.framework;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;

import javax.swing.JLabel;

/**
 *  A string that can extend over multiple lines.
 */
public class MultiLineString implements Cloneable
{
	public static final int LEFT = 0;
	public static final int CENTER = 1;
	public static final int RIGHT = 2;
	public static final int LARGE = 3;
	public static final int NORMAL = 4;

	private String aText = "";
	private int aJustification = CENTER;
	private boolean aBold = false;
	private boolean aUnderlined = false;
	private JLabel aLabel = new JLabel();
	
	/**
     * Constructs an empty, centered, normal size multi-line
     * string that is not underlined and not bold.
	 */
	public MultiLineString() 
	{}
	
	/**
     * Constructs an empty, centered, normal size multi-line
     * string that is not underlined. pBold determines if it is bold.
     * @param pBold True if the string should be bold.
	 */
	public MultiLineString(boolean pBold) 
	{ 
		aBold = pBold;
	}
	
	/**
     * Sets the value of the text property.
     * @param pText the text of the multiline string
	 */
	public void setText(String pText)
	{ 
		aText = pText; 
		setLabelText(); 
	}
   
	/**
     * Gets the value of the text property.
     * @return the text of the multi-line string
	 */
	public String getText() 
	{ 
		return aText; 
	}
   
	/**
     * Sets the value of the justification property.
     * @param pJustification the justification, one of LEFT, CENTER, RIGHT
	 */
	public void setJustification(int pJustification) 
	{ 
		aJustification = pJustification; 
		setLabelText(); 
	}
   
	/**
     * Gets the value of the justification property.
     * @return the justification, one of LEFT, CENTER, RIGHT
	 */
	public int getJustification() 
	{ 
		return aJustification;
	}
   
	/**
     * Gets the value of the underlined property.
     * @return true if the text is underlined
	 */
	public boolean isUnderlined() 
	{ 
		return aUnderlined;
	}
	
	/**
	 * @return The value of the bold property.
	 */
	public boolean isBold()
	{
		return aBold;
	}
  
	/**
     * Sets the value of the underlined property.
     * @param pUnderlined true to underline the text
	 */
	public void setUnderlined(boolean pUnderlined) 
	{ 
		aUnderlined = pUnderlined; 
		setLabelText(); 
	}
   
	@Override
	public String toString()
	{
		return aText.replace('\n', '|');
	}

	private void setLabelText()
	{
		StringBuffer htmlText = convertToHtml();
      
		aLabel.setText(htmlText.toString());
		if(aJustification == LEFT)
		{
			aLabel.setHorizontalAlignment(JLabel.LEFT);
		}
		else if(aJustification == CENTER)
		{
			aLabel.setHorizontalAlignment(JLabel.CENTER);
		}
		else if(aJustification == RIGHT) 
		{
			aLabel.setHorizontalAlignment(JLabel.RIGHT);
		}
	}

	/*
	 * @return an HTML version of the text of the string,
	 * taking into account the properties (underline, bold, etc.)
	 */
	StringBuffer convertToHtml()
	{
		StringBuffer prefix = new StringBuffer();
		StringBuffer suffix = new StringBuffer();
		StringBuffer htmlText = new StringBuffer();
		
		// Add some spacing before and after the text.
		prefix.append("&nbsp;");
		suffix.insert(0, "&nbsp;");
		if(aUnderlined) 
		{
			prefix.append("<u>");
			suffix.insert(0, "</u>");
		}
		if(aBold)
		{
			prefix.append("<b>");
			suffix.insert(0, "</b>");
		}

		htmlText.append("<html>");
		StringTokenizer tokenizer = new StringTokenizer(aText, "\n");
		boolean first = true;
		while(tokenizer.hasMoreTokens())
		{
			if(first) 
			{
				first = false;
			}
			else 
			{
				htmlText.append("<br>");
			}
			htmlText.append(prefix);
			String next = tokenizer.nextToken();
			String next1 = next.replaceAll("<", "&lt;");
			String next2 = next1.replaceAll(">", "&gt;");
			htmlText.append(next2);
			htmlText.append(suffix);
		}      
		htmlText.append("</html>");
		return htmlText;
	}
   
	/**
     * Gets the bounding rectangle for this multiline string.
     * @param pGraphics2D the graphics context
     * @return the bounding rectangle (with top left corner (0,0))
	 */
	public Rectangle2D getBounds(Graphics2D pGraphics2D)
	{
		if(aText.length() == 0) 
		{
			return new Rectangle2D.Double();
		}
		// setLabelText();
		Dimension dim = aLabel.getPreferredSize();       
		return new Rectangle2D.Double(0, 0, dim.getWidth(), dim.getHeight());
	}

	/**
     * Draws this multiline string inside a given rectangle.
     * @param pGraphics2D the graphics context
     * @param pRectangle the rectangle into which to place this multiline string
	 */
	public void draw(Graphics2D pGraphics2D, Rectangle2D pRectangle)
	{
		// setLabelText();
		aLabel.setFont(pGraphics2D.getFont());
		aLabel.setBounds(0, 0, (int) pRectangle.getWidth(), (int) pRectangle.getHeight());
		pGraphics2D.translate(pRectangle.getX(), pRectangle.getY());
		aLabel.paint(pGraphics2D);
		pGraphics2D.translate(-pRectangle.getX(), -pRectangle.getY());        
	}
	
	/**
	 * Returns true if the two have the same properties.
	 * Returns false otherwise
	 * @param pString The MultiLineString to check against
	 * @return true or false if the values are equal
	 */
	public boolean equalProperties(MultiLineString pString)
	{
		if(!aText.equals(pString.getText()))
		{
			return false;
		}
		if(!(aJustification == pString.getJustification()))
		{
			return false;
		}
		if(!(aBold == pString.aBold))
		{
			return false;
		}
		if(!(aUnderlined == pString.isUnderlined()))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public Object clone()
	{
		try
		{
			MultiLineString cloned = (MultiLineString) super.clone();
			cloned.aLabel = new JLabel();
			cloned.setLabelText();
			return cloned;
		}
		catch (CloneNotSupportedException exception)
		{
			return null;
		}
	}
}
