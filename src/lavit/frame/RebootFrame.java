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

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import lavit.Env;
import lavit.FrontEnd;
import lavit.localizedtext.MsgID;

@SuppressWarnings("serial")
public class RebootFrame extends JDialog
{
	private SelectPanel panel;

	public RebootFrame()
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("LaViT Reboot");
		setIconImages(Env.getApplicationIcons());
		setAlwaysOnTop(true);
		setResizable(false);

		panel = new SelectPanel(this);
		add(panel);

		addWindowListener(new ChildWindowListener(this));

		pack();
		setLocationRelativeTo(FrontEnd.mainFrame);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setVisible(true);
			}
		});
	}

	private class SelectPanel extends JPanel implements ActionListener
	{
		private Window frame;

		private JTextField pathInput = new JTextField();

		private JButton reboot = new JButton(Env.getMsg(MsgID.menu_reboot));
		private JButton cancel = new JButton(Env.getMsg(MsgID.text_cancel));

		public SelectPanel(Window frame)
		{
			this.frame = frame;

			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

			JPanel inputPanel = new JPanel();

			JLabel label = new JLabel();
			label.setText("Memory");
			inputPanel.add(label);

			pathInput.setPreferredSize(new Dimension(100,20));
			inputPanel.add(pathInput);

			add(inputPanel);


			JPanel buttonPanel = new JPanel();

			reboot.addActionListener(this);
			buttonPanel.add(reboot);

			cancel.addActionListener(this);
			buttonPanel.add(cancel);

			add(buttonPanel);

			if(getMemory().equals("")){
				pathInput.setText("512M");
			}else{
				pathInput.setText(getMemory());
			}

		}

		public void actionPerformed(ActionEvent e) {
			Object src = e.getSource();
			if(src==cancel){
				frame.dispose();
			}else if(src==reboot){
				Env.set("REBOOT_MAX_MEMORY",pathInput.getText());
				frame.dispose();
				FrontEnd.reboot();
			}

		}

		private String getMemory(){
			String memory = Env.get("REBOOT_MAX_MEMORY");
			if(memory==null){
				return "";
			}else{
				return memory;
			}
		}

	}
}
