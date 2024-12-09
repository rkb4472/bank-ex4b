import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

record BankRecords(Collection<Owner> owners, Collection<Account> accounts, Collection<RegisterEntry> registerEntries) { }

public class Obfuscator {
    private static Logger logger = LogManager.getLogger(Obfuscator.class.getName());

    public BankRecords obfuscate(BankRecords rawObjects) {
        // TODO: Obfuscate and return the records! Fill these in with your own
        // Example: mask SSN
        List<Owner> newOwners = new ArrayList<>(rawObjects.owners());
        List<Account> newAccounts = new ArrayList<>(rawObjects.accounts());
        Map<Long, Long> ownerIdMap = new HashMap<>();
        for (Owner o : rawObjects.owners()) {
            String new_ssn = "***-**-" + o.ssn().substring(7);
            // other changes...

            // replace name with hashed version
            String new_name = Integer.toHexString(o.name().hashCode());

            // replace address
            String new_address = "" + (int)(Math.random() * 1000);
            
            GregorianCalendar calendar = new GregorianCalendar();
        
            // shift dob
            int randomYear = 1900 + (int) (Math.random() * 124);
            int randomMonth = 1 + (int) (Math.random() * 12);
            int randomDay = 1 + (int) (Math.random() * 28);
            //String changed_year = String.format("%04d-%02d-%02d", randomYear, randomMonth, randomDay);
            calendar.set(randomYear, randomMonth, randomDay);
            Date new_year = calendar.getTime();

            SecureRandom secureRandom = new SecureRandom();
            long new_id = Math.abs(secureRandom.nextLong());
            ownerIdMap.put(o.id(), new_id);

            Owner newOwner = new Owner(new_name, new_id, new_year, new_ssn, new_address, o.address2(), o.city(), o.state(), o.zip());
            //newOwners.add(new Owner(new_name, new_id, new_year, new_ssn, new_address, o.address2(), o.city(), o.state(), o.zip()));
            ObfuscatorUtils.replaceRecord(o.id(), newOwner, newOwners);
            System.out.println("yyyy " + newOwner.id() + " " + new_id);

            // List<Account> accountsForOwner = ObfuscatorUtils.findRecords(o.id(), rawObjects.accounts());
            // System.out.println("xx " + accountsForOwner);
            // for (Account acc : accountsForOwner) {
            //     Account new_account;
            //     if(acc instanceof SavingsAccount savings) {
            //         String new_account_name = Integer.toHexString(savings.getName().hashCode());
            //         //long new_owner_id = savings.getOwnerId() * secureRandom.nextLong(); //need to change!!!
            //         new_account = new SavingsAccount(new_account_name, savings.getId(), savings.getBalance(), savings.getInterestRate(), new_id);
            //         System.out.println("xxxxx " + new_account + " new_id: " + new_id + " " + new_account.getId());
            //     } else if(acc instanceof CheckingAccount checking) {
            //         String new_account_name = Integer.toHexString(checking.getName().hashCode());
            //         //long new_owner_id = checking.getOwnerId() * secureRandom.nextLong(); // is it ok i modifid checkingaccount
            //         new_account = new CheckingAccount(new_account_name, checking.getId(), checking.getBalance(), checking.getCheckNumber(), new_id);
            //     } else {
            //         new_account = acc;
            //     }

            //     // replace updated acc
            //     ObfuscatorUtils.replaceRecord(acc.getId(), new_account, newAccounts);

            // }

        }

            for (Account acc : rawObjects.accounts()) {
                Account new_account;
                Long new_id = ownerIdMap.get(acc.getOwnerId());

                List<Owner> accountsOwner = ObfuscatorUtils.findRecords(new_id, newOwners);

                Owner matched_owner = accountsOwner.get(0);
                
                if(acc instanceof SavingsAccount savings) {
                    String new_account_name = Integer.toHexString(savings.getName().hashCode());
                    //long new_owner_id = savings.getOwnerId() * secureRandom.nextLong(); //need to change!!!
                    new_account = new SavingsAccount(new_account_name, savings.getId(), savings.getBalance(), savings.getInterestRate(), matched_owner.getId());
                    System.out.println("xxxxx " + new_account + " new_id: " + new_id + " " + new_account.getId());
                } else if(acc instanceof CheckingAccount checking) {
                    String new_account_name = Integer.toHexString(checking.getName().hashCode());
                    //long new_owner_id = checking.getOwnerId() * secureRandom.nextLong(); // is it ok i modifid checkingaccount
                    new_account = new CheckingAccount(new_account_name, checking.getId(), checking.getBalance(), checking.getCheckNumber(), matched_owner.getId());
                } else {
                    new_account = acc;
                }

                // replace updated acc
                ObfuscatorUtils.replaceRecord(acc.getId(), new_account, newAccounts);

            }

        //savings and checking accounts
        // final SecureRandom secureRandom = new SecureRandom();
        // List<Account> newAccounts = new ArrayList<>();
        // for (Account acc : rawObjects.accounts()) {
        //     if(acc instanceof SavingsAccount savings) {
        //         String new_name = Integer.toHexString(savings.getName().hashCode());
        //         long new_owner_id = savings.getOwnerId() * secureRandom.nextLong(); //need to change!!!
        //         newAccounts.add(new SavingsAccount(new_name, savings.getId(), savings.getBalance(), savings.getInterestRate(), new_owner_id));
        //     } else if(acc instanceof CheckingAccount checking) {
        //         String new_name = Integer.toHexString(checking.getName().hashCode());
        //         long new_owner_id = checking.getOwnerId() * secureRandom.nextLong(); // is it ok i modifid checkingaccount
        //         newAccounts.add(new CheckingAccount(new_name, checking.getId(), checking.getBalance(), checking.getCheckNumber(), new_owner_id));
        //     } else {
        //         newAccounts.add(acc);
        //     }
        // }

        // registers
        // List<RegisterEntry> newRegisters = new ArrayList<>();
        // for (RegisterEntry eg : rawObjects.registerEntries()) {
        //     SecureRandom random = new SecureRandom();
        //     double new_amount = eg.amount() * random.nextDouble(); // need to change

        //     int offset = random.nextInt(61) - 30; // +- 30 days
        //     Calendar calendar = Calendar.getInstance();
        //     calendar.setTime(eg.getDate());
        //     calendar.add(Calendar.DAY_OF_MONTH, offset);
        //     Date new_date = calendar.getTime();

        //     newRegisters.add(new RegisterEntry(eg.getId(), eg.accountId(), eg.entryName(), new_amount, new_date));
            

        // }
        // neeed total balance of register entries 
        List<RegisterEntry> newRegisters = new ArrayList<>(rawObjects.registerEntries()); 
        double totalAmount = newRegisters.stream()
            .mapToDouble(RegisterEntry::amount)
            .sum();

        SecureRandom random = new SecureRandom();
        double obfuscatedTotal = 0.0;
        Map<Long, Double> accountBalanceUpdates = new HashMap<>();

        // all but last
        for (int i = 0; i < newRegisters.size() - 1; i++) {
            RegisterEntry entry = newRegisters.get(i);

            // random multiplier between 0.8 and 1.2 for obfuscation
            double factor = 0.8 + (0.4) * random.nextDouble();
            double obfuscatedAmount = entry.amount() * factor;

            // to compare against total
            obfuscatedTotal += obfuscatedAmount;

            // keep track of balances
            accountBalanceUpdates.put(entry.accountId(), accountBalanceUpdates.getOrDefault(entry.accountId(), 0.0) + obfuscatedAmount);

            int offset = random.nextInt(61) - 30; // +- 30 days
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(entry.getDate());
            calendar.add(Calendar.DAY_OF_MONTH, offset);
            Date new_date = calendar.getTime();

            // new register entry
            RegisterEntry obfuscatedEntry = new RegisterEntry(
                entry.getId(), entry.accountId(), entry.entryName(), obfuscatedAmount, new_date
            );
                newRegisters.set(i, obfuscatedEntry); 
            }

            // deal w last entry
            RegisterEntry lastEntry = newRegisters.get(newRegisters.size() - 1);
            double lastObfuscatedAmount = totalAmount - obfuscatedTotal;
            accountBalanceUpdates.put(lastEntry.accountId(), accountBalanceUpdates.getOrDefault(lastEntry.accountId(), 0.0) + lastObfuscatedAmount);

            //date stuff for last entry
            int offset = random.nextInt(61) - 30; // +- 30 days
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastEntry.getDate());
            calendar.add(Calendar.DAY_OF_MONTH, offset);
            Date new_date = calendar.getTime();

            // add lat entry
            RegisterEntry finalObfuscatedEntry = new RegisterEntry(
                lastEntry.getId(), lastEntry.accountId(), lastEntry.entryName(), lastObfuscatedAmount, new_date
            );
            newRegisters.set(newRegisters.size() - 1, finalObfuscatedEntry);

            // have to update accounts
            for (Map.Entry<Long, Double> entry : accountBalanceUpdates.entrySet()) {
                long accountId = entry.getKey();
                double newBalance = entry.getValue();
                List<Account> accounts = ObfuscatorUtils.findRecords(accountId, newAccounts);
                Account acct = accounts.get(0);
                acct.setBalance(newBalance); // had to add a set balance method
            }

                
            Collection<Owner> obfuscatedOwners = newOwners;
            Collection<Account> obfuscatedAccounts = newAccounts;
            Collection<RegisterEntry> obfuscatedRegisterEntries = newRegisters;

        return new BankRecords(obfuscatedOwners, obfuscatedAccounts, obfuscatedRegisterEntries);
    }

    /**
     * Change the integration test suite to point to our obfuscated production
     * records.
     *
     * To use the original integration test suite files run
     *   "git checkout -- src/test/resources/persister_integ.properties"
     */
    public void updateIntegProperties() throws IOException {
        Properties props = new Properties();
        File propsFile = new File("src/test/resources/persister_integ.properties".replace('/', File.separatorChar));
        if (! propsFile.exists() || !propsFile.canWrite()) {
            throw new RuntimeException("Properties file must exist and be writable: " + propsFile);
        }
        try (InputStream propsStream = new FileInputStream(propsFile)) {
            props.load(propsStream);
        }
        props.setProperty("persisted.suffix", "_prod");
        logger.info("Updating properties file '{}'", propsFile);
        try (OutputStream propsStream = new FileOutputStream(propsFile)) {
            String comment = String.format(
                    "Note: Don't check in changes to this file!!\n" +
                    "#Modified by %s\n" +
                    "#to reset run 'git checkout -- %s'",
                    this.getClass().getName(), propsFile);
            props.store(propsStream, comment);
        }
    }

    public static void main(String[] args) throws Exception {
        // enable assertions
        Obfuscator.class.getClassLoader().setClassAssertionStatus("Obfuscator", true);
        logger.info("Loading Production Records");
        Persister.setPersisterPropertiesFile("persister_prod.properties");
        Bank bank = new Bank();
        bank.loadAllRecords();

        logger.info("Obfuscating records");
        Obfuscator obfuscator = new Obfuscator();
        // Make a copy of original values so we can compare length
        // deep-copy collections so changes in obfuscator don't impact originals
        BankRecords originalRecords = new BankRecords(
                new ArrayList<>(bank.getAllOwners()),
                new ArrayList<>(bank.getAllAccounts()),
                new ArrayList<>(bank.getAllRegisterEntries()));
        BankRecords obfuscatedRecords = obfuscator.obfuscate(originalRecords);

        logger.info("Saving obfuscated records");
        obfuscator.updateIntegProperties();
        Persister.resetPersistedFileNameAndDir();
        Persister.setPersisterPropertiesFile("persister_integ.properties");
        // old version of file is cached so we need to override prefix (b/c file changed
        // is not the one on classpath)
        Persister.setPersistedFileSuffix("_prod");
        // writeReords is cribbed from Bank.saveALlRecords(), refactor into common
        // method?
        Persister.writeRecordsToCsv(obfuscatedRecords.owners(), "owners");
        Map<Class<? extends Account>, List<Account>> splitAccounts = obfuscatedRecords
                .accounts()
                .stream()
                .collect(Collectors.groupingBy(rec -> rec.getClass()));
        Persister.writeRecordsToCsv(splitAccounts.get(SavingsAccount.class), "savings");
        Persister.writeRecordsToCsv(splitAccounts.get(CheckingAccount.class),"checking");
        Persister.writeRecordsToCsv(obfuscatedRecords.registerEntries(), "register");

        logger.info("Original   record counts: {} owners, {} accounts, {} registers",
                originalRecords.owners().size(),
                originalRecords.accounts().size(),
                originalRecords.registerEntries().size());
        logger.info("Obfuscated record counts: {} owners, {} accounts, {} registers",
                obfuscatedRecords.owners().size(),
                obfuscatedRecords.accounts().size(),
                obfuscatedRecords.registerEntries().size());

        if (obfuscatedRecords.owners().size() != originalRecords.owners().size())
            throw new AssertionError("Owners count mismatch");
        if (obfuscatedRecords.accounts().size() != originalRecords.accounts().size())
            throw new AssertionError("Account count mismatch");
        if (obfuscatedRecords.registerEntries().size() != originalRecords.registerEntries().size())
            throw new AssertionError("RegisterEntries count mismatch");
    }
}
