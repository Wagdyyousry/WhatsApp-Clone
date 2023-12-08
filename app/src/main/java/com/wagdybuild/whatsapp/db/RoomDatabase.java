package com.wagdybuild.whatsapp.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import com.wagdybuild.whatsapp.models.DBFriendMessage;
import com.wagdybuild.whatsapp.models.DBGroupMessage;
import com.wagdybuild.whatsapp.models.Group;
import com.wagdybuild.whatsapp.models.User;

@Database(entities = {User.class, Group.class, DBFriendMessage.class, DBGroupMessage.class}, version = 1)
public abstract class RoomDatabase extends androidx.room.RoomDatabase {
    public abstract RoomDAO roomDAO();
    private static volatile RoomDatabase INSTANCE;
    public static RoomDatabase getINSTANCE(final Context context) {
        if (INSTANCE == null) {
            synchronized (RoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),RoomDatabase.class, "RoomDatabase")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
