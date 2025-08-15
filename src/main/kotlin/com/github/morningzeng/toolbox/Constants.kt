package com.github.morningzeng.toolbox

import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.ui.JBColor
import java.time.format.DateTimeFormatter
import javax.swing.Icon

/**
 * @author Morning Zeng
 * @since 2025-05-16
 */
object Constants {

    object DateTimeFormatterC {
        val YYYY_MM_DD_HH_MM_SS: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val YYYY_MM_DD: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val YYYY_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val HH_MM_SS: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    @Suppress("unused")
    object IconC {

        val CLASS_LOADER: ClassLoader = IconC::class.java.getClassLoader()

        fun getIcon(expUIPath: String, path: String): Icon {
            return if (JBColor.isBright()) {
                getIcon(path, CLASS_LOADER)
            } else {
                getIcon(expUIPath, CLASS_LOADER)
            }
        }

        val ADD: Icon = getIcon("/images/svg/add.svg", CLASS_LOADER)
        val ADD_DRAWER: Icon = getIcon("/images/svg/add_drawer.svg", CLASS_LOADER)
        val AUTORENEW: Icon = getIcon("/images/svg/autorenew.svg", CLASS_LOADER)
        val BOX: Icon = getIcon("/images/svg/box.svg", CLASS_LOADER)
        val CLOCK_COLOR: Icon = getIcon("/images/svg/clock_color.svg", CLASS_LOADER)
        val DISABLE_MINUS: Icon = getIcon("images/svg/disable_minus_dark.svg", "images/svg/disable_minus.svg")
        val DOUBLE_ANGLES_DOWN: Icon = getIcon("/images/svg/double_angles_down.svg", CLASS_LOADER)
        val DOUBLE_ANGLES_UP: Icon = getIcon("/images/svg/double_angles_up.svg", CLASS_LOADER)
        val DOUBLE_ANGLES_RIGHT: Icon = getIcon("/images/svg/double_angles_right.svg", CLASS_LOADER)
        val DOUBLE_ANGLES_LEFT: Icon = getIcon("/images/svg/double_angles_left.svg", CLASS_LOADER)
        val FOLDER_COLOR: Icon = getIcon("/images/svg/folder_color.svg", CLASS_LOADER)
        val FORMAT_CODE: Icon = getIcon("/images/svg/format_code.svg", CLASS_LOADER)
        val GENERATE: Icon = getIcon("/images/svg/generate.svg", CLASS_LOADER)
        val INK_ERASER: Icon = getIcon("/images/svg/ink_eraser.svg", CLASS_LOADER)
        val REMOVE_RED: Icon = getIcon("/images/svg/remove.svg", CLASS_LOADER)
        val SAVE: Icon = getIcon("/images/svg/save.svg", CLASS_LOADER)
        val SAVE_ALL: Icon = getIcon("/images/svg/save_all.svg", CLASS_LOADER)
        val SECURITY: Icon = getIcon("/images/svg/security.svg", CLASS_LOADER)
        val SIGNATURE: Icon = getIcon("/images/svg/signature.svg", CLASS_LOADER)
        val STYLUS_NOTE: Icon = getIcon("/images/svg/stylus_note.svg", CLASS_LOADER)
        val TREE_NODE: Icon = getIcon("/images/svg/tree_node.svg", CLASS_LOADER)
    }
}