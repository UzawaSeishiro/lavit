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

package lavit.frame;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import lavit.Env;

public final class LanguageSetting
{
	private LanguageSetting() { }

	public static boolean showDialog()
	{
		LanguageSelectPanel sp = new LanguageSelectPanel();
		ModalSettingDialog dialog = ModalSettingDialog.createDialog(sp);
		dialog.setDialogTitle("Language");
		dialog.setHeadLineText("Language");
		dialog.setDescriptionText("Please select your language.");
		dialog.setDialogResizable(false);
		dialog.setDialogIconImages(Env.getApplicationIcons());

		boolean approved = dialog.showDialog();
		if (approved)
		{
			Env.set("LANG", sp.getSelectedLang());
		}
		return approved;
	}
}

@SuppressWarnings("serial")
class LanguageSelectPanel extends JPanel
{
	private String[] labels = { "English (en)", "Japanese (ja)" };
	private String[] langs = { "en", "ja" };
	private JRadioButton[] radios = new JRadioButton[labels.length];

	public LanguageSelectPanel()
	{
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < labels.length; i++)
		{
			radios[i] = new JRadioButton(labels[i]);
			group.add(radios[i]);
			add(radios[i]);
		}
		radios[0].setSelected(true);

		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		Group hg = gl.createParallelGroup(Alignment.LEADING);
		Group vg = gl.createSequentialGroup();
		for (JRadioButton radio : radios)
		{
			hg.addComponent(radio);
			vg.addComponent(radio);
		}
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
	}

	public String getSelectedLang()
	{
		for (int i = 0; i < labels.length; i++)
		{
			if (radios[i].isSelected())
			{
				return langs[i];
			}
		}
		return "";
	}
}
