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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lavit.*;
import lavit.stateviewer.StateGraphPanel;
import lavit.stateviewer.StatePanel;
import lavit.stateviewer.StateTransitionAbstraction;
import lavit.stateviewer.worker.State3DDynamicMover;
import lavit.stateviewer.worker.StateDynamicMover;
import lavit.stateviewer.worker.StateGraphExchangeWorker;
import lavit.util.CommonFontUser;
import lavit.util.FixFlowLayout;

public class StateDynamicControlPanel extends JPanel implements ChangeListener,ActionListener {

	private StatePanel statePanel;
	private StateDynamicMover mover;

	private JPanel dynamicPanel = new JPanel();
	private JCheckBox dynamicModeling = new JCheckBox("Dynamic Modeling");
	private JButton stretchMoveButton = new JButton("Stretch Move");

	private JPanel parameterPanel = new JPanel();
	private JLabel springLabel = new JLabel();
	private JSlider springSlider = new JSlider(0,100);
	private JLabel nodeRepulsionLabel = new JLabel();
	private JSlider nodeRepulsionSlider = new JSlider(0,100);
	private JLabel dummyRepulsionLabel = new JLabel();
	private JSlider dummyRepulsionSlider = new JSlider(0,100);
	private JLabel intervalLabel = new JLabel();
	private JSlider intervalSlider = new JSlider(0,100);
	private JLabel maxSpeedLabel = new JLabel();
	private JSlider maxSpeedSlider = new JSlider(0,100);
	private JLabel labels[] = {springLabel,nodeRepulsionLabel,dummyRepulsionLabel,intervalLabel,maxSpeedLabel};
	private JSlider sliders[] = {springSlider,nodeRepulsionSlider,dummyRepulsionSlider,intervalSlider,maxSpeedSlider};

	StateDynamicControlPanel(StatePanel statePanel){

		this.statePanel = statePanel;
		this.mover = statePanel.stateGraphPanel.getDynamicMover();

		springSlider.setValue(Env.getInt("SV_DYNAMIC_SPRING"));
		nodeRepulsionSlider.setValue(Env.getInt("SV_DYNAMIC_NODE_REPULSION"));
		dummyRepulsionSlider.setValue(Env.getInt("SV_DYNAMIC_DUMMY_REPULSION"));
		intervalSlider.setValue(Env.getInt("SV_DYNAMIC_INTERVAL"));
		maxSpeedSlider.setValue(Env.getInt("SV_DYNAMIC_MAXSPEED"));
		stateChanged(null);

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		dynamicPanel.setLayout(new GridLayout(1,2));
		dynamicPanel.setMaximumSize(new Dimension(1000,30));
		//dynamicPanel.setBorder(new TitledBorder("Dynamic Modeling"));
		dynamicPanel.setBorder(new EmptyBorder(2,5,2,5));
		dynamicModeling.addActionListener(this);
		dynamicModeling.setSelected(Env.is("SV_DYNAMIC_MOVER"));
		dynamicPanel.add(dynamicModeling);
		stretchMoveButton.addActionListener(this);
		dynamicPanel.add(stretchMoveButton);
		add(dynamicPanel);

		parameterPanel.setLayout(new GridLayout(labels.length, 2));
		parameterPanel.setMaximumSize(new Dimension(1000,140));
		parameterPanel.setBorder(new TitledBorder("Parameter"));
		for(int i=0;i<labels.length;++i){
			parameterPanel.add(labels[i]);
			sliders[i].addChangeListener(this);
			parameterPanel.add(sliders[i]);
		}
		add(parameterPanel);
	}

	public void setEnabled(boolean enabled){
		super.setEnabled(enabled);
		for(JSlider slider : sliders){
			slider.setEnabled(enabled);
		}
	}

	public void stateChanged(ChangeEvent e) {
		mover.setInnerSpring(springSlider.getValue());
		Env.set("SV_DYNAMIC_SPRING", springSlider.getValue());
		mover.setInnerNodeRepulsion(nodeRepulsionSlider.getValue());
		Env.set("SV_DYNAMIC_NODE_REPULSION", nodeRepulsionSlider.getValue());
		mover.setInnerDummyRepulsion(dummyRepulsionSlider.getValue());
		Env.set("SV_DYNAMIC_DUMMY_REPULSION", dummyRepulsionSlider.getValue());
		mover.setInnerInterval(intervalSlider.getValue());
		Env.set("SV_DYNAMIC_INTERVAL", intervalSlider.getValue());
		mover.setInnerMaxSpeed(maxSpeedSlider.getValue());
		Env.set("SV_DYNAMIC_MAXSPEED", maxSpeedSlider.getValue());
		stateUpdate();
		if(statePanel.state3DPanel==null){ return; }
		State3DDynamicMover mover3d = statePanel.state3DPanel.mover;
		mover3d.setInnerSpring(springSlider.getValue());
		mover3d.setInnerNodeRepulsion(nodeRepulsionSlider.getValue());
		mover3d.setInnerDummyRepulsion(dummyRepulsionSlider.getValue());
		mover3d.setInnerInterval(intervalSlider.getValue());
		mover3d.setInnerMaxSpeed(maxSpeedSlider.getValue());
	}

	public void stateUpdate(){
		springLabel.setText(" Spring constant : "+springSlider.getValue());
		nodeRepulsionLabel.setText(" Node Repulsion constant : "+nodeRepulsionSlider.getValue());
		dummyRepulsionLabel.setText(" Dummy Repulsion constant : "+dummyRepulsionSlider.getValue());
		intervalLabel.setText(" Interval : "+intervalSlider.getValue());
		maxSpeedLabel.setText(" Max Speed : "+maxSpeedSlider.getValue());
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if(src==dynamicModeling){
			Env.set("SV_DYNAMIC_MOVER",!Env.is("SV_DYNAMIC_MOVER"));
			statePanel.stateGraphPanel.setDynamicMoverActive(Env.is("SV_DYNAMIC_MOVER"));
		}else if(src==stretchMoveButton){
			statePanel.stateGraphPanel.stretchMove();
		}
	}

	/*
	public void setSpringSliderPos(int pos){
		if(pos<1){ pos=1; }else if(pos>100){ pos=100; }
		springSlider.removeChangeListener(this);
		springSlider.setValue(pos);
		springSlider.addChangeListener(this);
		stateUpdate();
	}

	public void setRepulsionSliderPos(int pos){
		if(pos<1){ pos=1; }else if(pos>100){ pos=100; }
		repulsionSlider.removeChangeListener(this);
		repulsionSlider.setValue(pos);
		repulsionSlider.addChangeListener(this);
		stateUpdate();
	}
	*/

}
