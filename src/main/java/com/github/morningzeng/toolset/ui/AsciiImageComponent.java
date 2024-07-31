package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.CollapsibleTitledSeparator;
import com.github.morningzeng.toolset.component.ImageLabel;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.asciiimage.AsciiImgCache;
import com.github.morningzeng.toolset.utils.asciiimage.AsciiToImageConverter;
import com.github.morningzeng.toolset.utils.asciiimage.ColorSquareErrorFitStrategy;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.JBFont;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Morning Zeng
 * @since 2024-07-30
 */
@Slf4j
public final class AsciiImageComponent extends JBPanel<JBPanelWithEmptyText> {

    private final Project project;
    private final AsciiImgCache asciiImgCache = AsciiImgCache.create(JBFont.create(new Font("Courier", Font.BOLD, 6)));
    private final CollapsibleTitledSeparator paramSeparator = new CollapsibleTitledSeparator("Request Parameter");
    private final LabeledComponent<TextFieldWithBrowseButton> imageComponent = LabeledComponent.create(
            new TextFieldWithBrowseButton(new ExtendableTextField(20)), "Image path", BorderLayout.WEST
    );
    private final LabeledComponent<JBIntSpinner> qualityComponent = LabeledComponent.create(
            new JBIntSpinner(3, 1, 100), "Image quality", BorderLayout.WEST
    );

    private final JBPanel<JBPanelWithEmptyText> parametersPanel = new JBPanel<>();
    private final JBSplitter splitter = new JBSplitter(false, "", 0.3f, 0.7f);

    private final ImageLabel sourceImageLabel = new ImageLabel(500, 500);
    private final ImageLabel asciiImageLabel = new ImageLabel(500, 500);

    public AsciiImageComponent(final Project project) {
        this.project = project;

        this.initLayout();
        this.initEvent();
    }

    private void initLayout() {
        this.setLayout(new GridBagLayout());

        GridBagUtils.builder(this.parametersPanel)
                .newRow(row -> {
                    final ExtendableTextField textField = (ExtendableTextField) imageComponent.getComponent().getTextField();
                    textField.getEmptyText().setText("Image path");
                    row.fill(GridBagFill.HORIZONTAL)
                            .newCell().add(this.imageComponent)
                            .newCell().add(this.qualityComponent);
                })
                .build();

        GridBagUtils.builder(this)
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().add(this.paramSeparator))
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().add(this.parametersPanel))
                .newRow(row -> {
                    this.splitter.setDividerWidth(3);
                    this.splitter.setFirstComponent(this.sourceImageLabel);
                    this.splitter.setSecondComponent(this.asciiImageLabel);
                    row.fill(GridBagFill.BOTH)
                            .newCell().weightX(1).weightY(1).add(this.splitter);
                })
                .build();
    }

    private void initEvent() {
        this.paramSeparator.addExpandedListener(this.parametersPanel::setVisible);
        this.imageComponent.getComponent().addBrowseFolderListener("Select Image", "Select image", this.project, FileChooserDescriptorFactory.createSingleFileDescriptor());
        this.imageComponent.getComponent().getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                this.update(e);
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                this.update(e);
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                this.update(e);
            }

            @SneakyThrows
            void update(final DocumentEvent e) {
                final String text = e.getDocument().getText(0, e.getDocument().getLength());
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        renderAsciiImage(text);
                    } catch (Exception ex) {
                        ApplicationManager.getApplication().invokeLater(
                                () -> Messages.showErrorDialog(ex.getMessage(), "Render Ascii Image Error")
                        );
                        log.error("Render Ascii Image Error", ex);
                    }
                });
            }
        });
    }

    private void renderAsciiImage(final String text) throws IOException {
        if (StringUtil.isEmpty(text)) {
            return;
        }
        final int quality = this.qualityComponent.getComponent().getNumber();
        final BufferedImage sourceImage = Thumbnails.of(text)
                .scale(quality)
                .asBufferedImage();
        this.sourceImageLabel.setImage(sourceImage);

        final AsciiToImageConverter converter = new AsciiToImageConverter(this.asciiImgCache, new ColorSquareErrorFitStrategy());
        this.asciiImageLabel.setImage(converter.convertImage(sourceImage));
    }

}
