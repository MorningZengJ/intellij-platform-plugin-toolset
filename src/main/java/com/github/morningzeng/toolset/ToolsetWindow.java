package com.github.morningzeng.toolset;

import com.github.morningzeng.toolset.ui.enums.TabEnum;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Morning Zeng
 * @since 2024-05-08
 */
public class ToolsetWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(final @NotNull Project project, final @NotNull ToolWindow toolWindow) {
//        final JBTabbedPane tabbedPane = new JBTabbedPane();
//        Arrays.stream(TabEnum.values()).forEach(tab -> tab.putTab(project, tabbedPane));
        final ContentFactory instance = ContentFactory.getInstance();
        for (final TabEnum tab : TabEnum.values()) {
            if (tab.load()) {
                final Content content = instance.createContent(tab.component(project), tab.title(), false);
                toolWindow.getContentManager().addContent(content);
            }
        }
    }

}
