package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.ImageLabel;
import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.GridBagUtils;
import com.github.morningzeng.toolset.utils.GridBagUtils.GridBagFill;
import com.github.morningzeng.toolset.utils.qrcode.AbstractQRCode;
import com.github.morningzeng.toolset.utils.qrcode.QRCodeFillTypeEnum;
import com.google.common.collect.Lists;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.colorpicker.ColorButton;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.fields.ExtendableTextField;

import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Morning Zeng
 * @since 2024-07-22
 */
public final class QRCodeComponent extends JBSplitter {
    private final Project project;

    private final LabeledComponent<JBIntSpinner> widthSpinner = LabeledComponent.create(
            new JBIntSpinner(500, 1, 9999), "Width", BorderLayout.WEST
    );
    private final LabeledComponent<JBIntSpinner> heightSpinner = LabeledComponent.create(
            new JBIntSpinner(500, 1, 9999), "Height", BorderLayout.WEST
    );
    private final LabeledComponent<ComboBox<String>> formatComboBox = LabeledComponent.create(
            new ComboBox<>(new String[]{"JPEG", "PNG", "GIF"}), "Format", BorderLayout.WEST
    );
    private final LabeledComponent<ColorButton> onColorButton = LabeledComponent.create(
            new ColorButton(JBColor.BLACK), "Foreground color", BorderLayout.WEST
    );
    private final LabeledComponent<ColorButton> offColorButton = LabeledComponent.create(
            new ColorButton(JBColor.WHITE), "Background color", BorderLayout.WEST
    );
    private final LabeledComponent<ColorButton> logoStrokeColorButton = LabeledComponent.create(
            new ColorButton(JBColor.WHITE), "Logo stroke color", BorderLayout.WEST
    );
    private final LabeledComponent<JBIntSpinner> marginSpinner = LabeledComponent.create(
            new JBIntSpinner(2, 0, 50), "Margins", BorderLayout.WEST
    );
    private final LabeledComponent<JBIntSpinner> strokeSpinner = LabeledComponent.create(
            new JBIntSpinner(5, 0, 100), "Logo stroke", BorderLayout.WEST
    );
    private final LabeledComponent<ComboBox<ErrorCorrectionLevel>> errorCorrectionLevelComboBox = LabeledComponent.create(
            new ComboBox<>(ErrorCorrectionLevel.values()), "Error correction level", BorderLayout.WEST
    );
    private final LabeledComponent<ComboBox<Charset>> charsetComboBox = LabeledComponent.create(
            new ComboBox<>(new Charset[]{
                    StandardCharsets.UTF_8, StandardCharsets.UTF_16, StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE, StandardCharsets.US_ASCII, StandardCharsets.ISO_8859_1
            }), "Charset", BorderLayout.WEST
    );
    private final LabeledComponent<TextFieldWithBrowseButton> logoTextField = LabeledComponent.create(
            new TextFieldWithBrowseButton(new ExtendableTextField(20)), "Logo path", BorderLayout.WEST
    );
    private final LabeledComponent<ComboBox<SymbolShapeHint>> dataMatrixShapeComboBox = LabeledComponent.create(
            new ComboBox<>(SymbolShapeHint.values()), "Data Matrix shape", BorderLayout.WEST
    );
    private final LabeledComponent<JBIntSpinner> logoRoundRectSpinner = LabeledComponent.create(
            new JBIntSpinner(5, 0, 100), "Round rectangle", BorderLayout.WEST
    );
    private final LabeledComponent<ComboBox<QRCodeFillTypeEnum>> fillTypeComboBox = LabeledComponent.create(
            new ComboBox<>(QRCodeFillTypeEnum.values()), "QR Code fill type", BorderLayout.WEST
    );
    private final LanguageTextArea contentTextArea;
    private final ImageLabel qrCodeLabel = new ImageLabel(500, 500);
    private final JButton generateQRCodeButton = new JButton("Generate QR Code");
    private final JButton copyBase64Button = new JButton("Covert to Base64 and Copy");

    public QRCodeComponent(final Project project) {
        super(false, "qr-code-splitter", .5f, .95f);
        this.setDividerWidth(3);
        this.project = project;

        this.contentTextArea = new LanguageTextArea(project);
        this.contentTextArea.setPlaceholder("The content of the QR code");

        this.initEvent();
        this.initLayout();
    }

    private void initEvent() {
        this.bindChooseColor(this.onColorButton.getComponent(), this.offColorButton.getComponent(), this.logoStrokeColorButton.getComponent());
        this.logoTextField.getComponent().addBrowseFolderListener(
                "Select Logo File", "Select logo file", this.project, FileChooserDescriptorFactory.createSingleFileDescriptor()
        );
        this.generateQRCodeButton.addActionListener(e -> this.generateQRCodeImage());
        this.copyBase64Button.addActionListener(e -> this.generateQRCodeImage(image -> {
            final String base64 = this.buildQRCode().toBase64(image);
            try {
                final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(base64), null);
            } catch (Exception ex) {
                Messages.showMessageDialog(base64, "Convert QR Code to Base64 and Copy", Actions.DiffWithClipboard);
            }
        }));
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                int height = QRCodeComponent.this.getHeight();

                final Dimension dimension = new Dimension(qrCodeLabel.getWidth(), height - 50);
                qrCodeLabel.setSize(dimension);
                qrCodeLabel.setMaximumSize(dimension);
                qrCodeLabel.setPreferredSize(dimension);
                QRCodeComponent.this.repaint();
            }
        });
    }

    private void initLayout() {
        final JBPanel<JBPanelWithEmptyText> propPanel = GridBagUtils.builder()
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().add(
                                GridBagUtils.builder()
                                        .newRow(innerRow -> innerRow.fill(GridBagFill.HORIZONTAL)
                                                .newCell().add(this.onColorButton)
                                                .newCell().add(this.offColorButton)
                                                .newCell().weightX(.3).add(this.widthSpinner)
                                                .newCell().add(this.heightSpinner)
                                                .newCell().add(this.marginSpinner))
                                        .build()
                        ))
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().add(
                                GridBagUtils.builder()
                                        .newRow(innerRow -> innerRow.fill(GridBagFill.HORIZONTAL)
                                                .newCell().weightX(.3).add(this.fillTypeComboBox)
                                                .newCell().add(this.formatComboBox)
                                                .newCell().add(this.charsetComboBox)
                                                .newCell().add(this.errorCorrectionLevelComboBox))
                                        .build()
                        ))
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().add(
                                GridBagUtils.builder()
                                        .newRow(innerRow -> innerRow.fill(GridBagFill.HORIZONTAL)
                                                .newCell().add(this.logoStrokeColorButton)
                                                .newCell().add(this.strokeSpinner)
                                                .newCell().add(this.logoRoundRectSpinner)
                                                .newCell().weightX(1).add(this.logoTextField))
                                        .build()
                        ))
                .newRow(row -> row.fill(GridBagFill.BOTH)
                        .newCell().weightX(1).weightY(1).add(this.contentTextArea))
                .build();
        this.setFirstComponent(propPanel);

        final JBPanel<JBPanelWithEmptyText> panel = GridBagUtils.builder()
                .newRow(row -> row.fill(GridBagFill.BOTH).newCell().weightX(1).weightY(1).add(this.qrCodeLabel))
                .newRow(row -> row.fill(GridBagFill.HORIZONTAL)
                        .newCell().add(GridBagUtils.builder()
                                .newRow(crow -> crow.fill(GridBagFill.HORIZONTAL).newCell().add(this.generateQRCodeButton).newCell().add(this.copyBase64Button))
                                .build())
                )
                .build();
        this.setSecondComponent(panel);
    }

    private void bindChooseColor(final ColorButton... colorButtons) {
        for (final ColorButton colorButton : colorButtons) {
            colorButton.addActionListener(e -> {
                final Color chooseColor = ColorPicker.showDialog(
                        this, "Choose Color", colorButton.getColor(), true, Lists.newArrayList(), true
                );
                Optional.ofNullable(chooseColor).ifPresent(colorButton::setColor);
            });
        }
    }

    private void generateQRCodeImage() {
        generateQRCodeImage(bufferedImage -> {
        });
    }

    private void generateQRCodeImage(final Consumer<BufferedImage> consumer) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                final String content = this.contentTextArea.getText();
                if (StringUtil.isEmpty(content)) {
                    throw new IllegalArgumentException("The content of the QR code cannot be empty");
                }
                this.generateQRCodeButton.setEnabled(false);
                final AbstractQRCode built = this.buildQRCode();
                final String logoPath = this.logoTextField.getComponent().getText();
                final BufferedImage bufferedImage = StringUtil.isEmpty(logoPath) ? built.toBufferedImage(content) : built.toBufferedImage(content, logoPath);
                this.qrCodeLabel.setImage(bufferedImage);
                consumer.accept(bufferedImage);
            } catch (Exception ex) {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(ex.getMessage(), "Generate QR Code Error"));
            } finally {
                this.generateQRCodeButton.setEnabled(true);
            }
        });
    }

    private <T extends AbstractQRCode> T buildQRCode() {
        //noinspection unchecked
        return (T) this.fillTypeComboBox.getComponent().getItem().builder()
                .onColor(this.onColorButton.getComponent().getColor())
                .offColor(this.offColorButton.getComponent().getColor())
                .width(this.widthSpinner.getComponent().getNumber())
                .height(this.heightSpinner.getComponent().getNumber())
                .format(this.formatComboBox.getComponent().getItem())
                .margin(this.marginSpinner.getComponent().getNumber())
                .stroke(this.strokeSpinner.getComponent().getNumber())
                .level(this.errorCorrectionLevelComboBox.getComponent().getItem())
                .dataMatrixShape(this.dataMatrixShapeComboBox.getComponent().getItem())
                .strokeColor(this.logoStrokeColorButton.getComponent().getColor())
                .roundRectX(this.logoRoundRectSpinner.getComponent().getNumber())
                .roundRectY(this.logoRoundRectSpinner.getComponent().getNumber())
                .build();
    }

}
