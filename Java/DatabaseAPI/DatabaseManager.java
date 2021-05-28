package muck.server.database;

import akka.actor.*;

import muck.core.Id;
import muck.protocol.connection.*;
import muck.protocol.connection.DatabaseMappings.*;
import muck.server.Broker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager extends AbstractActor {

    /** all DOAs required to match to any object/table mapping*/
    private static GeneralDAO<DatabaseEntityTestTwo> databaseEntityTestTwoDAO = new GeneralDAO<>("some_table", DatabaseEntityTestTwo.class);
    private static GeneralDAO<BankAccounts> bankAccountsDAO = new GeneralDAO<>("BankAccounts", BankAccounts.class);
    private static GeneralDAO<CharacterLocation> characterLocationDAO = new GeneralDAO<>("CharacterLocation", CharacterLocation.class);
    private static GeneralDAO<CharacterParameters> characterParametersDAO = new GeneralDAO<>("CharacterParameters", CharacterParameters.class);
    private static GeneralDAO<CharacterProfile> characterProfileDAO = new GeneralDAO<>("CharacterProfile", CharacterProfile.class);
    private static GeneralDAO<ChatArchive> chatArchiveDAO = new GeneralDAO<>("ChatArchive", ChatArchive.class);
    private static GeneralDAO<EventLog> eventLogDAO = new GeneralDAO<>("EventLog", EventLog.class);
    private static GeneralDAO<GuildRelationships> guildRelationshipsDAO = new GeneralDAO<>("GuildRelationships", GuildRelationships.class);
    private static GeneralDAO<Guilds> guildsDAO = new GeneralDAO<>("Guilds", Guilds.class);
    private static GeneralDAO<Items> itemsDAO = new GeneralDAO<>("Items", Items.class);
    private static GeneralDAO<LiveActions> liveActionsGDAO = new GeneralDAO<>("LiveActions", LiveActions.class);
    private static GeneralDAO<MailDatabase> mailDatabaselDAO = new GeneralDAO<>("MailDatabase", MailDatabase.class);
    private static GeneralDAO<UserInventory> userInventoryDAO = new GeneralDAO<>("UserInventory", UserInventory.class);
    private static GeneralDAO<UserRelationships> userRelationshipsDAO = new GeneralDAO<>("UserRelationships", UserRelationships.class);
    private static GeneralDAO<Users> usersDAO = new GeneralDAO<>("Users", Users.class);
    private static GeneralDAO<Sprites> spritesDAO = new GeneralDAO<>("Sprites", Sprites.class);

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    /** Send messages to the router so they can be sent to the client  */
    private ActorRef router;

    public static Props props() {
        return Props.create(Broker.class, Broker::new);
    }

    @Override
    public void preStart() {
        ActorSelection selection = getContext().actorSelection("/user/router");
        selection.tell(new Identify("router"), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorIdentity.class, id -> {
                    if(id.getActorRef().isPresent()) {
                        router = id.getActorRef().get();
                        getContext().watch(router);
                        logger.info("watching {}", router.path().toStringWithoutAddress());

                    }
                    else {
                        logger.error("DatabaseManager could not establish a connection to the router");
                    }
                })
                //match POJOs that map to database tables for updates
                .match(DatabaseEntityTestTwo.class, this::handleDatabaseAddEntityTestTwo)
                .match(BankAccounts.class, this::handleBankAccounts)
                .match(CharacterLocation.class, this::handleCharacterLocation)
                .match(CharacterParameters.class, this::handleCharacterParameters)
                .match(CharacterProfile.class, this::handleCharacterProfile)
                .match(ChatArchive.class, this::handleChatArchive)
                .match(EventLog.class, this::handleEventLog)
                .match(GuildRelationships.class, this::handleDGuildRelationships)
                .match(Guilds.class, this::handleGuilds)
                .match(Items.class, this::handleItems)
                .match(LiveActions.class, this::handleLiveActions)
                .match(MailDatabase.class, this::handleMailDatabase)
                .match(UserInventory.class, this::handleUserInventory)
                .match(UserRelationships.class, this::handleUserRelationships)
                .match(Users.class, this::handleUsers)
                .match(Sprites.class, this::handleSprites)

                //match DatabaseAccessEvent object for all database retrievals
                .match(DatabaseAccessEvent.class, this::handleDatabaseAccessEvent)

                //match DatabaseUpdateEvent object for all database retrievals
                .match(DatabaseUpdateEvent.class, this::handleDatabaseUpdateEvent)

                .matchAny(
                        o -> logger.info("Worker received a message {}", o)
                )
                .build();
    }

    /**
     * Handles all row requests by checking the expected Class of return type and
     * then selecting the appropriate DAO from the dataAccessObject array
     * @param message
     */
    private void handleDatabaseAccessEvent(DatabaseAccessEvent message) {
        try{
            if(message.getClassName().equals("Users")){
                switch (message.getTransactionId()){
                    case 0:
                        //get a row from primary key
                        Users entity = usersDAO.getRow(message.getPrimaryKeyVal(), message.getUserId());
                        router.tell(entity, self());
                    case 1:
                        // get a row from userName
                        String[] condition = { "userName = '" + message.getPrimaryKeyVal()[0] + "'"};
                        ArrayList<Users> entity1 = usersDAO.getRows(message.getUserId(), new ArrayList<>(), condition);
                        for (Users ent :entity1) {
                            sender().tell(ent, self());
                        }
                    default:
                        break;
                }

            }
            else if(message.getClassName().equals("ChatArchive")){
                switch (message.getTransactionId()){
                    case 0:
                        //get a row based on primary key
                        ChatArchive entity0 = chatArchiveDAO.getRow(message.getPrimaryKeyVal(), message.getUserId());
                        sender().tell(entity0, self());
                        break;
                    case 1:
                        ArrayList<ChatArchive> entities1 = chatArchiveDAO.getRows(message.getUserId(), new ArrayList<>(), message.getConditions());
                        sender().tell(new ChatArchive.List(entities1), self());
                        break;
                    default:
                        break;
                }
            }else if(message.getClassName().equals("UserInventory")){
                switch (message.getTransactionId()){
                    case 0:
                        // get a row based on primary key
                        UserInventory entity0 = userInventoryDAO.getRow(message.getPrimaryKeyVal(), message.getUserId());
                        sender().tell(entity0, self());
                        break;
                    case 1:
                        // get one or more rows based on a different query (see chatArchive code above for demo)
                        break;
                    case 2:
                        // get one or more rows based on another different query (see chatArchive code above for demo)
                        break;
                    default:
                        break;
                }
            }else if(message.getClassName().equals("Sprites")){
                switch (message.getTransactionId()){
                    case 0:
                        // get a row based on primary key
                        Sprites entity0 = userInventoryDAO.getRow(message.getPrimaryKeyVal(), message.getUserId());
                        sender().tell(entity0, self());
                        break;
                    case 1:
                        // get one or more rows based on a different query (see chatArchive code above for demo)
                        break;
                    case 2:
                        // get one or more rows based on another different query (see chatArchive code above for demo)
                        break;
                    default:
                        break;
                }
            }else if(message.getClassName().equals("GuildRelationships")){
                switch (message.getTransactionId()) {
                    case 1:
                        // get a list of a users guilds
                        ArrayList<GuildRelationships> entities = guildRelationshipsDAO.getRows(message.getUserId(), new ArrayList<>(), message.getConditions());
                        sender().tell(new GuildRelationships.List(entities), self());
                        break;
                    case 2:
                        break;
                }
            }
        } catch (SQLException e){
            sendDatabaseVerification(false, message.getUserId());
        }
    }

    private void handleDatabaseUpdateEvent(DatabaseUpdateEvent message) {
        Boolean result = null;
        Map<String, Object> conditions = new HashMap<>();
        ArrayList<String> separators = new ArrayList<>();
        if (message.getClassName().equals("DatabaseEntityTestTwo")) {
            switch (message.getTransactionId()){
                case 2:
                    // key/value pair of conditions
                    conditions.put("someInt", 3);
                    // Separators to define behaviour of conditions
                    // EXAMPLE: UPDATE.....WHERE id = 2 AND quantity > 5; Hence required "=" and ">"
                    separators.add("=");
                    result = databaseEntityTestTwoDAO.update(message.getUpdates(), conditions, separators, message.getTransactionId());
                    break;
                default:
                    break;
            }
            sendDatabaseVerification(result, message.getUserId());

        }
        else if(message.getClassName().equals("UserInventory")){
            int id = message.getTransactionId();
            if (id == 0 || id == 1 || id == 2){
               conditions.put("userID", message.getUserID());
               separators.add("=");
               // EXAMPLE: IDColumn = IDColumn + 1 or IDColumn = IDColumn - 1 or  IDColumn = !IDColumn
               result = userInventoryDAO.update(message.getUpdates(), conditions, separators, message.getTransactionId());
             } else if (id == 3){
               // another kind of update
             }
            sendDatabaseVerification(result, message.getUserId());
        }
    }


    // Handle add row requests for database mapping objects

    private void handleDatabaseAddEntityTestTwo(DatabaseEntityTestTwo message) {

        Boolean result = databaseEntityTestTwoDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleBankAccounts(BankAccounts message) {
        Boolean result = bankAccountsDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleCharacterLocation(CharacterLocation message) {
        Boolean result = characterLocationDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleCharacterParameters(CharacterParameters message) {
        Boolean result = characterParametersDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleCharacterProfile(CharacterProfile message) {
        Boolean result = characterProfileDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleChatArchive(ChatArchive message) {
        Boolean result = chatArchiveDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleEventLog(EventLog message) {
        Boolean result = eventLogDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleDGuildRelationships(GuildRelationships message) {
        Boolean result = guildRelationshipsDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleGuilds(Guilds message) {
        Boolean result = guildsDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleItems(Items message) {
        Boolean result = itemsDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleLiveActions(LiveActions message) {
        Boolean result = liveActionsGDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleMailDatabase(MailDatabase message) {
        Boolean result = mailDatabaselDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleUserInventory(UserInventory message) {
        Boolean result = userInventoryDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleUserRelationships(UserRelationships message) {
        Boolean result = userRelationshipsDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleUsers(Users message) {
        Boolean result = usersDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    private void handleSprites(Sprites message) {
        Boolean result = spritesDAO.saveRow(message);
        sendDatabaseVerification(result, message.getUserId());
    }

    /**
     * Creates a verification object to send to user for database query
     * @param success
     * @param userId
     */
    private void sendDatabaseVerification(Boolean success, Id<User> userId){
        if(success){
            sender().tell(new DatabaseVerificationEvent(userId, true, "Database Query Successful"), self());
        }else{
            sender().tell(new DatabaseVerificationEvent(userId, false, "Database Query Not Successful"), self());
        }
    }

}