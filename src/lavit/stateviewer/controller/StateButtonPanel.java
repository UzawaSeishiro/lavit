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

package lavit.stateviewer.controller;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

import lavit.Env;
import lavit.FrontEnd;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StateNode;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransitionAbstraction;
import lavit.stateviewer.worker.StateGraphExchangeWorker;

public class StateButtonPanel extends JPanel implements ActionListener {
	private StatePanel statePanel;

	private JPanel resetPanel = new JPanel();
	private JButton posReset = new JButton("Position Reset");
	private JButton adjustReset = new JButton("Adjust Reset");
	private JButton adjust2Reset = new JButton("Adjust(Backedge) Reset");
	private JButton adjust3Reset = new JButton("Adjust(Find) Reset");
	private JButton simpleMixAdjust = new JButton("Simple Mix Adjust");
	private JButton dummyMixAdjust = new JButton("Dummy Mix Adjust");

	private JPanel crossPanel = new JPanel();
	private JButton geneticAlgorithm = new JButton("Genetic Algorithm");
	private JButton exchangeReset = new JButton("Adjacent Exchange");
	private JCheckBox exchangeDummyOnly = new JCheckBox("Dummy Only");

	private JPanel dummyPanel = new JPanel();
	private JButton setBackDummy = new JButton("Set Back Dummy");
	private JButton setVerticalDummy = new JButton("Set Vertical Dummy");
	private JButton removeDummy = new JButton("Remove Dummy");
	private JButton dummyCentering = new JButton("Dummy Centering");
	private JButton dummySmoothing = new JButton("Dummy Smoothing");

	private JPanel abstractionPanel = new JPanel();
	private JButton transitionAbstraction = new JButton("Transition Abstraction");
	private JButton selectAbstraction = new JButton("Select Abstraction");

	private JPanel basicPanel = new JPanel();
	private JButton autoCentering = new JButton("Auto Centering");
	private JButton fitCentering = new JButton("Fit Centering");
	private JButton allReset = new JButton("All Reset");

	private JComponent comps[] = {
		posReset,adjustReset,adjust2Reset,adjust3Reset,simpleMixAdjust,dummyMixAdjust,
		geneticAlgorithm,exchangeReset,exchangeDummyOnly,
		setBackDummy,setVerticalDummy,removeDummy,dummyCentering,dummySmoothing,
		transitionAbstraction,selectAbstraction,
		autoCentering,fitCentering,allReset
	};

	StateButtonPanel(StatePanel statePanel){
		this.statePanel = statePanel;

		//setLayout(new GridLayout(5,1));
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		resetPanel.setLayout(new GridLayout(2,3));
		resetPanel.setBorder(new TitledBorder("Transformation"));
		posReset.addActionListener(this);
		resetPanel.add(posReset);
		adjustReset.addActionListener(this);
		resetPanel.add(adjustReset);
		adjust2Reset.addActionListener(this);
		resetPanel.add(adjust2Reset);
		adjust3Reset.addActionListener(this);
		resetPanel.add(adjust3Reset);
		simpleMixAdjust.addActionListener(this);
		resetPanel.add(simpleMixAdjust);
		dummyMixAdjust.addActionListener(this);
		resetPanel.add(dummyMixAdjust);
		add(resetPanel);

		crossPanel.setLayout(new GridLayout(1,3));
		crossPanel.setBorder(new TitledBorder("Cross Reduction"));
		geneticAlgorithm.addActionListener(this);
		crossPanel.add(geneticAlgorithm);
		exchangeReset.addActionListener(this);
		crossPanel.add(exchangeReset);
		exchangeDummyOnly.addActionListener(this);
		exchangeDummyOnly.setSelected(Env.is("SV_CROSSREDUCTION_DUMMYONLY"));
		crossPanel.add(exchangeDummyOnly);
		add(crossPanel);

		dummyPanel.setLayout(new GridLayout(1,5));
		dummyPanel.setBorder(new TitledBorder("Dummy Control"));
		setBackDummy.addActionListener(this);
		dummyPanel.add(setBackDummy);
		setVerticalDummy.addActionListener(this);
		dummyPanel.add(setVerticalDummy);
		removeDummy.addActionListener(this);
		dummyPanel.add(removeDummy);
		dummyCentering.addActionListener(this);
		dummyPanel.add(dummyCentering);
		dummySmoothing.addActionListener(this);
		dummyPanel.add(dummySmoothing);
		add(dummyPanel);

		abstractionPanel.setLayout(new GridLayout(1,2));
		abstractionPanel.setBorder(new TitledBorder("Abstraction"));
		transitionAbstraction.addActionListener(this);
		abstractionPanel.add(transitionAbstraction);
		selectAbstraction.addActionListener(this);
		abstractionPanel.add(selectAbstraction);
		add(abstractionPanel);

		basicPanel.setLayout(new GridLayout(1,3));
		autoCentering.addActionListener(this);
		basicPanel.add(autoCentering);
		fitCentering.addActionListener(this);
		basicPanel.add(fitCentering);
		allReset.addActionListener(this);
		basicPanel.add(allReset);
		add(basicPanel);

	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for(JComponent comp : comps){
			comp.setEnabled(enabled);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==posReset){
			statePanel.stateGraphPanel.positionReset();
			statePanel.stateGraphPanel.autoCentering();
		}else if(src==adjustReset){
			statePanel.stateGraphPanel.adjustReset();
		}else if(src==adjust2Reset){
			statePanel.stateGraphPanel.adjust2Reset();
		}else if(src==adjust3Reset){
			statePanel.stateGraphPanel.adjust3Reset();
		}else if(src==simpleMixAdjust){
			statePanel.stateGraphPanel.simpleMixAdjust();
		}else if(src==dummyMixAdjust){
			statePanel.stateGraphPanel.dummyMixAdjust();
		}else if(src==allReset){
			statePanel.reset();
		}
		else if(src==geneticAlgorithm){
			statePanel.stateGraphPanel.geneticAlgorithmLength();
		}else if(src==exchangeReset){
			statePanel.stateGraphPanel.exchangeReset();
		}else if(src==exchangeDummyOnly){
			Env.set("SV_CROSSREDUCTION_DUMMYONLY",!Env.is("SV_CROSSREDUCTION_DUMMYONLY"));
		}else if(src==setBackDummy){
			statePanel.stateGraphPanel.getDrawNodes().setBackDummy();
			statePanel.stateGraphPanel.getDrawNodes().dummyCentering();
			statePanel.stateGraphPanel.getDrawNodes().updateNodeLooks();
			statePanel.stateGraphPanel.update();
		}else if(src==setVerticalDummy){
			statePanel.stateGraphPanel.getDrawNodes().setVerticalDummy();
			statePanel.stateGraphPanel.getDrawNodes().updateNodeLooks();
			statePanel.stateGraphPanel.update();
		}else if(src==removeDummy){
			statePanel.stateGraphPanel.getDrawNodes().removeDummy();
			statePanel.stateGraphPanel.getDrawNodes().updateNodeLooks();
			statePanel.stateGraphPanel.selectClear();
			statePanel.stateGraphPanel.update();
		}else if(src==dummyCentering){
			statePanel.stateGraphPanel.getDrawNodes().dummyCentering();
			statePanel.stateGraphPanel.update();
		}else if(src==dummySmoothing){
			statePanel.stateGraphPanel.dummySmoothing();
			statePanel.stateGraphPanel.update();
		}else if(src==autoCentering){
			statePanel.stateGraphPanel.autoCentering();
		}else if(src==fitCentering){
			statePanel.stateGraphPanel.fitCentering();
		}else if(src==transitionAbstraction){
			StateGraphPanel p = statePanel.stateGraphPanel;
			new SelectStateTransitionRuleFrame(p,new StateTransitionAbstraction(p));
		}else if(src==selectAbstraction){
			statePanel.stateGraphPanel.selectNodeAbstraction();
		}
	}

}
