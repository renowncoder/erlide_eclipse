package org.erlide.ui.wizards

import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Combo
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage
import org.erlide.engine.model.root.IErlangProjectProperties
import org.erlide.engine.model.root.ProjectPreferencesConstants
import org.erlide.runtime.runtimeinfo.RuntimeVersion
import org.erlide.core.internal.builder.BuilderInfo

class ErlangNewProjectCreationPage extends WizardNewProjectCreationPage {
    val IErlangProjectProperties info

    new(String name, IErlangProjectProperties info) {
        super(name);
        this.info = info
    }

    override createControl(Composite parent) {
        super.createControl(parent)

        val composite = new Composite((control as Composite), SWT.NONE)
        composite.layout = new GridLayout(3, false)

        val label2 = new Label(composite, SWT.NONE)
        label2.text = 'Minimum Erlang version:'

        val version = new Combo(composite, SWT.READ_ONLY)
        val runtimeVersions = ProjectPreferencesConstants.SUPPORTED_VERSIONS
        version.setItems(runtimeVersions.map[toString])
        version.setText(ProjectPreferencesConstants.DEFAULT_RUNTIME_VERSION)
        version.addModifyListener [
            info.runtimeVersion = new RuntimeVersion(version.text)
        ]
        info.runtimeVersion = new RuntimeVersion(version.text)

        new Label(composite, SWT.NONE)

        val label = new Label(composite, SWT.NONE)
        label.text = 'Build system to be used:'

        val listener = new BuilderSelectionListener(info)
        val builders = BuilderInfo.values
        builders.forEach [ builder |
            var check = new Button(composite, SWT.RADIO)
            check.text = builder.toString.toLowerCase
            check.data = builder
            if (builder === BuilderInfo.INTERNAL) {
                check.selection = true
            }
            check.addSelectionListener(listener)
            val description = new Label(composite, SWT.NONE)
            description.text = getDescription(builder)
            new Label(composite, SWT.NONE)
        ]
        info.builderName = BuilderInfo.INTERNAL.toString.toUpperCase

    }

    override setVisible(boolean visible) {
        super.setVisible(visible)
        if (!visible) {
            info.name = projectName
            info.location = locationPath
        }
    }

    def String getDescription(BuilderInfo builder){
        switch(builder){
            case BuilderInfo.INTERNAL:
                ''': let erlide do the compiling.'''
            case BuilderInfo.MAKE:
                ''': choose this if there is a Makefile (even if it calls rebar or emake).'''
            case BuilderInfo.EMAKE:
                ''': straight Emake.'''
            case BuilderInfo.REBAR:
                ''': straight rebar.'''
        }
    }

}

class BuilderSelectionListener implements SelectionListener {

    val IErlangProjectProperties info

    new(IErlangProjectProperties info) {
        this.info = info
    }

    override widgetDefaultSelected(SelectionEvent e) {
    }

    override widgetSelected(SelectionEvent e) {
        info.builderName = (e.widget.data as BuilderInfo).toString
    }
}
