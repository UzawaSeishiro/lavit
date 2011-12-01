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
 */

package lavit.util;

import java.awt.Color;
import java.util.ArrayList;

public class ColorMaker {
	static ArrayList<Color> colors = null;

	public static void colorPreparation(){
		if(colors==null){
			colors = new ArrayList<Color>();
			for(int r : new int[]{0,128,255}){
				for(int g : new int[]{0,128,255}){
					for(int b : new int[]{0,128,255}){
						colors.add(new Color(r,g,b));
					}
				}
			}
		}
	}

	public static Color getColor(int i){
		colorPreparation();
		return colors.get(i%colors.size());
	}

	public static Color getColor(String str){
		int s=1;
		for(int i=0;i<str.length();++i){
			char c = str.charAt(i);
			if(isNumber(c)){
				if(i>=1&&isNumber(str.charAt(i-1))){ continue; }
				s+=1;
			}else if(c=='-'){
				s+=0;
			}else{
				s+=c;
			}
		}
		return getColor(s);
	}

	public static boolean isNumber(char c){
		if('0'<=c&&c<='9'){
			return true;
		}else{
			return false;
		}
	}

}