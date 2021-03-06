package info.vericoin.verimobile;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

public class MainActivity extends AppCompatActivity {

    private TextView unconfirmedBalance;
    private TextView availableBalance;
    private TextView blockHeight;
    private Button sendButton;
    private WalletAppKit kit;

    private CardView walletView;

    public static Intent createIntent(Context context){
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unconfirmedBalance = findViewById(R.id.unconfirmedBalance);
        availableBalance = findViewById(R.id.availableBalance);
        blockHeight = findViewById(R.id.blockHeight);
        sendButton = findViewById(R.id.sendButton);
        walletView = findViewById(R.id.wallet_card_view);

        walletView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(TransactionListActivity.createIntent(MainActivity.this));
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(SendActivity.createIntent(MainActivity.this));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        WalletConnection.connect(new WalletConnection.OnConnectListener() {

            @Override
            public void OnSetUpComplete(WalletAppKit kit) {
                MainActivity.this.kit = kit;

                setBalances(kit.wallet());
                setBlockHeight(kit.chain().getBestChainHeight());
            }
        });

        WalletConnection.setOnCoinReceiveListener(new WalletConnection.OnCoinReceiveListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                setBalances(wallet);
            }

            @Override
            public void onSuccess(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance, TransactionConfidence result) {
                setBalances(wallet);
            }

            @Override
            public void onFailure(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                setBalances(wallet);
            }
        });

        WalletConnection.setOnNewBestBlockListener(new WalletConnection.OnNewBestBlockListener() {
            @Override
            public void newBlock(StoredBlock block) {
                setBlockHeight(block.getHeight());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        WalletConnection.disconnect();
    }

    public void setBlockHeight(int height){
        blockHeight.setText(String.valueOf(height));
    }

    public void setBalances(Wallet wallet){
        Coin available = wallet.getBalance(Wallet.BalanceType.AVAILABLE);
        Coin estimated = wallet.getBalance(Wallet.BalanceType.ESTIMATED);
        Coin unconfirmed = estimated.subtract(available);
        setUnconfirmedBalance(unconfirmed);
        setAvailableBalance(available);
    }

    public void setUnconfirmedBalance(Coin coin){
        unconfirmedBalance.setText(coin.toFriendlyString());
    }

    public void setAvailableBalance(Coin coin){
        availableBalance.setText(coin.toFriendlyString());
    }
}
