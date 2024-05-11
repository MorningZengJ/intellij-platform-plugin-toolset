package com.github.morningzeng.toolset.ui;

import com.github.morningzeng.toolset.component.PlaceholderTextField;
import com.github.morningzeng.toolset.enums.CryptoTabEnum;
import com.github.morningzeng.toolset.utils.SymmetricCrypto;
import com.intellij.ide.plugins.newui.HorizontalLayout;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.GridBag;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.GridBagLayout;

/**
 * @author Morning Zeng
 * @since 2024-05-11
 */
public final class AESComponent extends JBPanel<JBPanelWithEmptyText> {

    final SymmetricCrypto[] cryptos = SymmetricCrypto.values();

    /**
     * The `keyTextField` variable is a private instance of the `PlaceholderTextField` class. It is used to allow user input for a key in the AESComponent class.
     * <p>
     * The `PlaceholderTextField` class is a custom text field component that provides a placeholder functionality. When the text field is empty and not focused, it displays a gray
     * placeholder text, which is the key in this case. When the text field is focused, the placeholder text disappears and the user can enter their own key.
     * <p>
     * The keyTextField has a length limit of 25 characters and has the initial placeholder text set to "Key".
     * <p>
     * To get the actual text entered by the user, you can call the `getText()` method of the `keyTextField` instance.
     * <p>
     * Example usage:
     * ```
     * String key = keyTextField.getText();
     * ```
     */
    private final PlaceholderTextField keyTextField = new PlaceholderTextField(25, "Key");
    /**
     * Represents a text field component with a placeholder functionality.
     * This component extends JBTextField and implements DocumentListener.
     * It allows displaying a placeholder text when the field is empty and the focus is not on it.
     */
    private final PlaceholderTextField ivTextField = new PlaceholderTextField(25, "IV");
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
    public AESComponent() {
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
        final GridBag gridBag = new GridBag();

        gridBag.fill = GridBag.HORIZONTAL;
        gridBag.weightx = 1;

        gridBag.gridx = 0;
        gridBag.gridy = 0;
        this.add(keyTextField, gridBag);

        gridBag.gridx = 1;
        gridBag.gridy = 0;
        this.add(ivTextField, gridBag);

        gridBag.gridx = 2;
        gridBag.gridy = 0;
        this.add(cryptoComboBox, gridBag);

        gridBag.fill = GridBag.BOTH;

        gridBag.gridx = 0;
        gridBag.gridy = 1;
        gridBag.weighty = 1.;
        this.add(decryptArea, gridBag);

        final JBPanel<JBPanelWithEmptyText> btnPanel = new JBPanel<>();
        btnPanel.setLayout(new HorizontalLayout(2));
        btnPanel.add(encryptBtn);
        btnPanel.add(decryptBtn);
        gridBag.gridx = 0;
        gridBag.gridy = 2;
        this.add(btnPanel, gridBag);

        gridBag.gridx = 0;
        gridBag.gridy = 3;
        this.add(encryptArea, gridBag);
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
                final SymmetricCrypto crypto = this.cryptos[this.cryptoComboBox.getSelectedIndex()];
                final String enc = crypto.crypto(this.keyTextField.getText(), this.ivTextField.getText()).enc(this.decryptArea.getText());
                this.encryptArea.setText(enc);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Encrypt Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        this.decryptBtn.addActionListener(e -> {
            try {
                final SymmetricCrypto crypto = this.cryptos[this.cryptoComboBox.getSelectedIndex()];
                final String dec = crypto.crypto(this.keyTextField.getText(), this.ivTextField.getText()).dec(this.encryptArea.getText());
                this.decryptArea.setText(dec);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Decrypt Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

}
