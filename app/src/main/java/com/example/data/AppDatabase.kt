package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.IncidentCategory
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val categoryName: String,
    val location: String,
    val approximateTime: String,
    val perpetratorDescription: String,
    val vehicleDetails: String,
    val narrative: String,
    val statusName: String,
    val officerName: String?,
    val badgeNumber: String?,
    val policeStation: String?,
    val filedTimestamp: Long,
    val encryptedPayloadHash: String,
    val qrCodePayload: String
) {
    fun toDomain(): Complaint {
        val cat = try {
            IncidentCategory.valueOf(categoryName)
        } catch (e: Exception) {
            IncidentCategory.HARASSMENT
        }
        val st = try {
            ComplaintStatus.valueOf(statusName)
        } catch (e: Exception) {
            ComplaintStatus.REGISTERED
        }
        return Complaint(
            id = id,
            category = cat,
            location = location,
            approximateTime = approximateTime,
            perpetratorDescription = perpetratorDescription,
            vehicleDetails = vehicleDetails,
            narrative = narrative,
            status = st,
            officerName = officerName,
            badgeNumber = badgeNumber,
            policeStation = policeStation,
            filedTimestamp = filedTimestamp,
            encryptedPayloadHash = encryptedPayloadHash,
            qrCodePayload = qrCodePayload
        )
    }

    companion object {
        fun fromDomain(domain: Complaint): ComplaintEntity {
            return ComplaintEntity(
                id = domain.id,
                categoryName = domain.category.name,
                location = domain.location,
                approximateTime = domain.approximateTime,
                perpetratorDescription = domain.perpetratorDescription,
                vehicleDetails = domain.vehicleDetails,
                narrative = domain.narrative,
                statusName = domain.status.name,
                officerName = domain.officerName,
                badgeNumber = domain.badgeNumber,
                policeStation = domain.policeStation,
                filedTimestamp = domain.filedTimestamp,
                encryptedPayloadHash = domain.encryptedPayloadHash,
                qrCodePayload = domain.qrCodePayload
            )
        }
    }
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY filedTimestamp DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(complaints: List<ComplaintEntity>)

    @Query("DELETE FROM complaints")
    suspend fun deleteAll()
}

@Database(entities = [ComplaintEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun complaintDao(): ComplaintDao
}
