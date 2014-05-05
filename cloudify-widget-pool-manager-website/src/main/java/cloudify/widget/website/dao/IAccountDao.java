package cloudify.widget.website.dao;

import cloudify.widget.website.models.AccountModel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/27/14
 * Time: 10:39 AM
 */
public interface IAccountDao {

    public Long createAccount( AccountModel account );

    public boolean deleteAccount( Long id );

    public AccountModel readAccountByUuid( String uuid );

    public List<AccountModel> readAccounts();


    public AccountModel readById( Long accountId );
}
