/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */package lavit.mcprofiler;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class MCProfileGraphPanel extends JPanel {

	MCProfilePanel profile;
	
	final Color cState = new Color(0,0,255);
	final Color cHashSize = new Color(0,200,0);
	final Color cHashConflict = new Color(255,0,0);
	
	boolean active;
	private int maxX = 5;
	private int maxY = 5;
	
	private MCProfileStatusPanel status;

	MCProfileGraphPanel(MCProfilePanel p){
		profile = p;
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		status = new MCProfileStatusPanel(this);
		add(status);
	}
	
	void start(){
		active = true;
		maxX = maxY = 5;
	}
	
	void end(){
		active = false;
	}
	
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		
		int pw = getWidth();
		int ph = getHeight();
		
		g2.setColor(Color.white);
		g2.fillRect(0, 0, pw, ph);
		
		if(active){
			status.setBackgroundColor(new Color(255,255,255,200));
		}else{
			status.setBackgroundColor(new Color(220,220,220,200));
		}
		
		int lM = 50;
		int rM = 10;
		int tM = 10;
		int bM = 30;
		int gw = pw - lM - rM;
		int gh = ph - tM - bM;
		
		if(gw<50||gh<50){ return; }
		
		while(profile.timeLine.size()>0&&profile.timeLine.get(profile.timeLine.size()-1)[0]>(maxY*9/10)){ maxY *= 2; }
		while(profile.timeLine.size()>maxX){ maxX+=5; }
		
		g2.setColor(Color.black);
		g2.drawLine(lM,ph-bM,pw-rM,ph-bM); //x
		g2.drawLine(lM,tM,lM,ph-bM); //y
		
		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('0');
		int charHeight = fm.getHeight();
		
		g2.drawString("0", lM-charWidth/2+1,ph-bM+3+charHeight);
		for(int x=1;x<=5;++x){
			g2.drawLine(lM+gw*x/5,ph-bM+2,lM+gw*x/5,ph-bM-2);
			String str = ""+maxX*x/5;
			g2.drawString(str, lM+gw*x/5-charWidth*str.length()/2+1,ph-bM+3+charHeight);
		}
		for(int y=1;y<=5;++y){
			g2.drawLine(lM+2,ph-bM-gh*y/5,lM-2,ph-bM-gh*y/5);
			String str = ""+maxY*y/5;
			g2.drawString(str, lM-charWidth*str.length()-3,ph-bM-gh*y/5+charHeight/2);
		}
		
		if(profile.timeLine.size()<2){ return; }
		
		
		for(int x=1;x<profile.timeLine.size();++x){
			g2.setColor(cHashConflict);
			g2.drawLine(lM+(x-1)*gw/maxX,ph-bM-profile.timeLine.get(x-1)[2]*gh/maxY,lM+(x)*gw/maxX,ph-bM-profile.timeLine.get(x)[2]*gh/maxY);
			g2.setColor(cHashSize);
			g2.drawLine(lM+(x-1)*gw/maxX,ph-bM-profile.timeLine.get(x-1)[1]*gh/maxY,lM+(x)*gw/maxX,ph-bM-profile.timeLine.get(x)[1]*gh/maxY);
			g2.setColor(cState);
			g2.drawLine(lM+(x-1)*gw/maxX,ph-bM-profile.timeLine.get(x-1)[0]*gh/maxY,lM+(x)*gw/maxX,ph-bM-profile.timeLine.get(x)[0]*gh/maxY);
		}
		
	}
	
}
