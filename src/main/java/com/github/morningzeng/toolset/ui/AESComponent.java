package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.config.LocalConfigFactory;
import com.github.morningzeng.toolset.config.LocalConfigFactory.SymmetricCryptoProp;
import com.github.morningzeng.toolset.dialog.SymmetricPropDialog;
import com.github.morningzeng.toolset.enums.CryptoTabEnum;
import com.github.morningzeng.toolset.listener.ContentBorderListener;
import com.github.morningzeng.toolset.utils.GridLayoutUtils;
import com.github.morningzeng.toolset.utils.SymmetricCrypto;
import com.intellij.icons.AllIcons.General;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.GridBag;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Morning Zeng
 * @since 2024-05-11
 */
public final class AESComponent extends JBPanel<JBPanelWithEmptyText> {

    private final static String TYPE = "AES";

    final SymmetricCrypto[] cryptos = Arrays.stream(SymmetricCrypto.values())
            .filter(crypto -> TYPE.equals(crypto.getType()))
            .toArray(SymmetricCrypto[]::new);
    final LocalConfigFactory localConfigFactory;
    final Project project;
    /**
     * Represents a combo box used for selecting a SymmetricCryptoProp.
     *
     * <p>
     * The cryptoPropComboBox is an instance of the ComboBox class, which allows the user to select a SymmetricCryptoProp from a list of available options.
     * It is used in the AESComponent class to provide a selection for cryptography algorithm options.
     * </p>
     *
     * <p>
     * The cryptoPropComboBox is initialized with the list of SymmetricCryptoProp instances obtained from the LocalConfigFactory class.
     * The data for the combo box is generated by converting the set of SymmetricCryptoProp objects to an array.
     * </p>
     *
     * <p>
     * The SymmetricCryptoProp class is a nested class of the LocalConfigFactory class.
     * It represents a cryptographic algorithm with properties such as key, initialization vector (IV), title, description, and sorting order.
     * This class is used to populate the combo box options.
     * </p>
     *
     * @see ComboBox
     * @see SymmetricCryptoProp
     * @see LocalConfigFactory
     * @see AESComponent
     */
    private final ComboBox<SymmetricCryptoProp> cryptoPropComboBox;
    /**
     * JButton for managing cryptographic operations.
     * <p>
     * This JButton is used in the AESComponent class for managing cryptographic operations.
     *
     * @see AESComponent
     */
    private final JButton cryptoManageBtn = new JButton(General.Ellipsis);
    /**
     * Represents a combo box for selecting the type of symmetric encryption algorithm.
     * <p>
     * This variable is an instance of the ComboBox class.
     * It is declared as 'private final', indicating that it cannot be modified or reassigned once initialized.
     * The generic type parameter is 'SymmetricCrypto', representing the type of items contained in the combo box.
     * <p>
     * The combo box is initialized with the cryptos items, which are options for selecting a symmetric encryption algorithm.
     * <p>
     * This combo box is used in the AESComponent class to allow the user to choose the encryption algorithm.
     *
     * @since <version>
     */
    private final ComboBox<SymmetricCrypto> cryptoComboBox = new ComboBox<>(this.cryptos);
    /**
     * The {@code encryptArea} variable is an instance of the {@code JBTextArea} class.
     * It is a private final field in the {@code AESComponent} class.
     * This field represents the text area where the encrypted data will be displayed.
     * <p>
     * The dimensions of the text area are set to 5 rows and 20 columns.
     * <p>
     * The initial value of the text area is empty.
     * <p>
     * This field is used along with other fields and methods in the {@code AESComponent} class to perform encryption operations.
     *
     * @see AESComponent
     * @see JBTextArea
     */
    private final JBTextArea encryptArea = new JBTextArea(5, 20);
    /**
     * TextArea used for displaying decrypted text.
     * <p>
     * This variable is declared as private and final, meaning it cannot be accessed or modified outside of the class where it is defined, and its value cannot be changed once set
     * .
     * <p>
     * The textarea has an initial size of 5 rows and 20 columns.
     * <p>
     * Note: This variable is a field of the AESComponent class, and can be accessed within this class only.
     */
    private final JBTextArea decryptArea = new JBTextArea(5, 20);
    /**
     * Button used to initiate the encryption process.
     */
    private final JButton encryptBtn = new JButton("Encrypt", IconLoader.getIcon("/images/svg/keyboard_double_arrow_down_24dp.svg", CryptoTabEnum.class.getClassLoader()));
    /**
     * Button used for decrypting data.
     * <p>
     * The decrypt button contains an icon and the text "Decrypt". When clicked,
     * it initiates the decryption process.
     * <p>
     * Note: The icon is loaded from a specific file path using the IconLoader class.
     * <p>
     * Example usage:
     * <p>
     * AESComponent aesComponent = new AESComponent();
     * JButton decryptBtn = aesComponent.getDecryptBtn();
     * decryptBtn.addActionListener(e -> {
     * // Perform decryption logic here
     * });
     */
    private final JButton decryptBtn = new JButton("Decrypt", IconLoader.getIcon("/images/svg/keyboard_double_arrow_up_24dp.svg", CryptoTabEnum.class.getClassLoader()));

    /**
     * Initializes a new instance of the AESComponent class.
     * <p>
     * This constructor initializes the layout and action listeners for the AESComponent.
     * It calls the initLayout() and initAction() methods to set up the UI components and their actions.
     */
    public AESComponent(final Project project) {
        this.project = project;
        this.localConfigFactory = LocalConfigFactory.getInstance();
        this.cryptoComboBox.setSelectedItem(SymmetricCrypto.AES_CBC_PKCS5);

        this.cryptoPropComboBox = new ComboBox<>(this.localConfigFactory.symmetricCryptos().stream()
                .sorted(Comparator.comparing(SymmetricCryptoProp::getSorted))
                .toArray(SymmetricCryptoProp[]::new));
        this.cryptoPropComboBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (Objects.isNull(value)) {
                return new JLabel();
            }
            final String template = "%s - %s ( %s / %s )";
            return new JLabel(template.formatted(value.getTitle(), value.getDesc(), value.getKey(), value.getIv()));
        });

        this.initLayout();
        this.initAction();
    }

    /**
     * Initializes the layout of the AESComponent class.
     * <p>
     * This method sets up the GridBagLayout for the AESComponent and adds various components to it.
     */
    private void initLayout() {
        this.setLayout(new GridBagLayout());

        final JBPanel<JBPanelWithEmptyText> btnPanel = new JBPanel<>();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.LINE_AXIS));
        btnPanel.add(encryptBtn);
        btnPanel.add(decryptBtn);

        this.decryptArea.addFocusListener(ContentBorderListener.builder().component(this.decryptArea).init());
        this.encryptArea.addFocusListener(ContentBorderListener.builder().component(this.encryptArea).init());
        this.decryptArea.setLineWrap(true);
        this.decryptArea.setWrapStyleWord(true);
        this.encryptArea.setLineWrap(true);
        this.encryptArea.setWrapStyleWord(true);

        final JBScrollPane decryptScrollPane = new JBScrollPane(this.decryptArea);
        final JBScrollPane encryptScrollPane = new JBScrollPane(this.encryptArea);
        decryptScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        decryptScrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        encryptScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        encryptScrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        GridLayoutUtils.builder()
                .container(this).fill(GridBag.HORIZONTAL).weightX(1).add(this.cryptoPropComboBox)
                .newCell().weightX(0).add(this.cryptoManageBtn)
                .newCell().add(this.cryptoComboBox)
                .newRow().fill(GridBag.BOTH).weightY(1).gridWidth(3).add(decryptScrollPane)
                .newRow().weightY(0).add(btnPanel)
                .newRow().weightY(1).gridWidth(3).add(encryptScrollPane);
    }

    /**
     * Initializes the actions for the AESComponent class.
     * <p>
     * This method sets up the ActionListener for the encryption button and the decryption button.
     * When the encryption button is clicked, it retrieves the selected cryptography algorithm from the combo box,
     * retrieves the key and IV from the corresponding text fields, and encrypts the text from the decryption area using the selected algorithm.
     * The encrypted text is then set in the encryption area.
     * <p>
     * When the decryption button is clicked, it retrieves the selected cryptography algorithm from the combo box,
     * retrieves the key and IV from the corresponding text fields, and decrypts the text from the encryption area using the selected algorithm.
     * The decrypted text is then set in the decryption area.
     * <p>
     * If any exception occurs during encryption or decryption, an error message dialog is displayed.
     */
    private void initAction() {
        this.encryptBtn.addActionListener(e -> {
            try {
                final SymmetricCrypto crypto = this.cryptoComboBox.getItem();
                final SymmetricCryptoProp cryptoProp = this.cryptoPropComboBox.getItem();
                final String enc = crypto.crypto(cryptoProp.getKey(), cryptoProp.getIv()).enc(this.decryptArea.getText());
                this.encryptArea.setText(enc);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Encrypt Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.decryptBtn.addActionListener(e -> {
            try {
                final SymmetricCrypto crypto = this.cryptoComboBox.getItem();
                final SymmetricCryptoProp cryptoProp = this.cryptoPropComboBox.getItem();
                final String dec = crypto.crypto(cryptoProp.getKey(), cryptoProp.getIv()).dec(this.encryptArea.getText());
                this.decryptArea.setText(dec);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Decrypt Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.cryptoManageBtn.addActionListener(e -> {
            final SymmetricPropDialog dialog = new SymmetricPropDialog(this.project, TYPE);
            dialog.showAndGet();
            this.refresh();
        });
    }

    void refresh() {
        this.cryptoPropComboBox.removeAllItems();
        this.localConfigFactory.symmetricCryptos().stream()
                .sorted(Comparator.comparing(SymmetricCryptoProp::getSorted))
                .forEach(this.cryptoPropComboBox::addItem);
    }

}
