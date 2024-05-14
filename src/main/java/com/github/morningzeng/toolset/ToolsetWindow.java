package com.github.morningzeng.toolset;

import com.github.morningzeng.toolset.enums.TabEnum;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Morning Zeng
 * @since 2024-05-08
 */
public class ToolsetWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(final @NotNull Project project, final @NotNull ToolWindow toolWindow) {
        final JBTabbedPane tabbedPane = new JBTabbedPane();
        Arrays.stream(TabEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
        final Content content = ContentFactory.getInstance().createContent(tabbedPane, "", false);
        toolWindow.getContentManager().addContent(content);
    }

}
