package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.LanguageTextArea;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.QrCodeUtils;
import com.google.common.collect.Lists;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBImageIcon;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

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
    private final LanguageTextArea contentTextArea;
    private final JBImageIcon imageIcon = new JBImageIcon(ImageUtil.createImage(500, 500, BufferedImage.TYPE_INT_ARGB));
    private final JBLabel qrCodeLabel = new JBLabel(this.imageIcon) {
        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            if (Objects.isNull(this.getIcon())) {
                return;
            }
            final JBImageIcon icon = (JBImageIcon) this.getIcon();
            final Image image = icon.getImage();
            final int size = Math.min(this.getWidth(), this.getHeight());
            g.setColor(UIManager.getColor("Label.background"));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.drawImage(image, 0, 0, size, size, this);
        }
    };
    private final JButton generateQRCodeButton = new JButton("Generate QR Code");

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
        this.generateQRCodeButton.addActionListener(e -> ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                final String content = this.contentTextArea.getText();
                if (StringUtil.isEmpty(content)) {
                    throw new IllegalArgumentException("The content of the QR code cannot be empty");
                }
                this.generateQRCodeButton.setEnabled(false);
                final QrCodeUtils built = buildQRCode();
                final String logoPath = this.logoTextField.getComponent().getText();
                final BufferedImage bufferedImage = StringUtil.isEmpty(logoPath) ? built.toBufferedImage(content) : built.toBufferedImageWithLogo(content, logoPath);
                this.imageIcon.setImage(bufferedImage);
                this.qrCodeLabel.setIcon(this.imageIcon);
                this.qrCodeLabel.repaint();
            } catch (Exception ex) {
                ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(ex.getMessage(), "Generate QR Code Error"));
            } finally {
                this.generateQRCodeButton.setEnabled(true);
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
        final JBPanel<JBPanelWithEmptyText> propPanel = new JBPanel<>(new GridBagLayout());

        GridLayoutUtils.builder()
                .container(propPanel).fill(GridBag.HORIZONTAL).add(this.linePanel(this.onColorButton, this.offColorButton, this.widthSpinner, this.heightSpinner, this.marginSpinner))
                .newRow().add(this.linePanel(this.formatComboBox, this.charsetComboBox, this.errorCorrectionLevelComboBox))
                .newRow().add(this.linePanel(this.logoTextField, this.strokeSpinner, this.logoRoundRectSpinner, this.logoStrokeColorButton))
                .newRow().fill(GridBag.BOTH).weightX(1).weightY(1).add(this.contentTextArea)
        ;

        this.setFirstComponent(propPanel);
        final JBPanel<JBPanelWithEmptyText> qrCodePanel = new JBPanel<>();
        qrCodePanel.setLayout(new BoxLayout(qrCodePanel, BoxLayout.Y_AXIS));
        qrCodePanel.add(this.qrCodeLabel);
        qrCodePanel.add(this.generateQRCodeButton);
        this.setSecondComponent(qrCodePanel);
    }

    private JBPanel<JBPanelWithEmptyText> linePanel(final Component... components) {
        final JBPanel<JBPanelWithEmptyText> panel = new JBPanel<>();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        for (final Component component : components) {
            panel.add(component);
        }
        return panel;
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

    private QrCodeUtils buildQRCode() {
        return QrCodeUtils.builder()
                .onColor(this.onColorButton.getComponent().getColor().getRGB())
                .offColor(this.offColorButton.getComponent().getColor().getRGB())
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
