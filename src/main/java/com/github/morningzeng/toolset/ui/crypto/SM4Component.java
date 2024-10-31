package com.github.morningzeng.toolset.ui.crypto;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SM4;
import com.github.morningzeng.toolset.model.SymmetricCryptoProp;
import com.github.morningzeng.toolset.utils.GridBagUtils.Row;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;

/**
 * @author Morning Zeng
 * @since 2024-10-31
 */
public final class SM4Component extends AbstractSymmetricCryptoComponent {

    private final ComboBox<Mode> modeComboBox = new ComboBox<>(Mode.values());
    private final ComboBox<Padding> paddingComboBox = new ComboBox<>(Padding.values());

    public SM4Component(final Project project) {
        super(project);
        this.initLayout();
        this.initAction();
    }

    @Override
    void cryptoRow(final Row<AbstractSymmetricCryptoComponent> row) {
        row.newCell().add(this.modeComboBox)
                .newCell().add(this.paddingComboBox);
    }

    @Override
    String enc(final SymmetricCryptoProp cryptoProp) {
        return this.sm4(cryptoProp).encryptHex(this.decryptArea.getText());
    }

    @Override
    String dec(final SymmetricCryptoProp cryptoProp) {
        return this.sm4(cryptoProp).decryptStr(this.encryptArea.getText());
    }

    private SM4 sm4(final SymmetricCryptoProp cryptoProp) {
        final Mode mode = this.modeComboBox.getItem();
        final Padding padding = this.paddingComboBox.getItem();
        final byte[] key = HexUtil.decodeHex(cryptoProp.getKey());
        final byte[] iv = HexUtil.decodeHex(cryptoProp.getIv());
        return new SM4(mode, padding, key, iv);
    }
}
