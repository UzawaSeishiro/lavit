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

package lavit.oldstateviewer.controller;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import lavit.Env;
import lavit.FrontEnd;
import lavit.Lang;
import lavit.frame.ChildWindowListener;
import lavit.oldstateviewer.*;
import lavit.util.StateTransitionCatcher;

public class SelectStateTransitionRuleFrame extends JFrame implements ActionListener {
	private StateTransitionCatcher catcher;

	private JPanel panel;
	private SelectPanel rulePanel;

	private JPanel btnPanel;
	private JButton ok;
	private JButton rev;

	private boolean end;
	private HashMap<String,ArrayList<StateTransition>> rules = new HashMap<String,ArrayList<StateTransition>>();

	public SelectStateTransitionRuleFrame(StateGraphPanel graphPanel,StateTransitionCatcher catcher){

		this.catcher = catcher;

		for(StateTransition t : graphPanel.getDrawNodes().getAllTransition()){
			for(String r : t.getRules()){
				if(!rules.containsKey(r)){
					rules.put(r,new ArrayList<StateTransition>());
				}
				rules.get(r).add(t);
			}
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Rules");
        setIconImage(Env.getImageOfFile(Env.IMAGEFILE_ICON));
        setAlwaysOnTop(true);
        setResizable(false);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        rulePanel = new SelectPanel();
        panel.add(rulePanel, BorderLayout.CENTER);


        btnPanel =  new JPanel();
        btnPanel.setLayout(new GridLayout(1,2));

        ok = new JButton(Lang.d[6]);
        ok.addActionListener(this);
        btnPanel.add(ok);

        rev = new JButton(Lang.d[8]);
        rev.addActionListener(this);
        btnPanel.add(rev);

        panel.add(btnPanel, BorderLayout.SOUTH);

        add(panel);

        addWindowListener(new ChildWindowListener(this));

        pack();
        setLocationRelativeTo(FrontEnd.mainFrame);
        setVisible(true);

        end = false;
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src==ok){
			ArrayList<StateTransition> trans = new ArrayList<StateTransition>();
			for(String ruleName : rulePanel.getSelectedRuleNames()){
				trans.addAll(rules.get(ruleName));
			}
			catcher.transitionCatch(rulePanel.getSelectedRuleNames(), trans);
			dispose();
		}else if(src==rev){
			rulePanel.selectReverse();
		}
	}

	public boolean isEnd(){
		return end;
	}

	public void end(){
		end = true;
	}

	private class SelectPanel extends JPanel {
		private JCheckBox[] ruleCheckBoxes;

		SelectPanel(){

			//�롼��̾�ν���
			ArrayList<String> rs = new ArrayList<String>(rules.keySet());
			Collections.sort(rs);

			setLayout(new GridLayout(rs.size(),2));

			ruleCheckBoxes = new JCheckBox[rs.size()];
			for(int i=0;i<rs.size();++i){
				ruleCheckBoxes[i] = new JCheckBox(rs.get(i));
				add(ruleCheckBoxes[i]);

				int c = 0;
				for(StateTransition t : rules.get(rs.get(i))){
					if(!t.isToDummy()){ c++; }
				}

				String cs = ""+c;
				if(c<1000){ cs = " "+cs; }
				if(c<100 ){ cs = " "+cs; }
				if(c<10  ){ cs = " "+cs; }
				add(new JLabel(" : "+cs+" transition(s) "));

			}

		}

		ArrayList<String> getSelectedRuleNames(){
			ArrayList<String> ruleNames = new ArrayList<String>();
			for(JCheckBox cb : ruleCheckBoxes){
				if(cb.isSelected()){
					ruleNames.add(cb.getText());
				}
			}
			return ruleNames;
		}

		void selectReverse(){
			for(JCheckBox cb : ruleCheckBoxes){
				cb.setSelected(!cb.isSelected());
			}
		}

	}
}