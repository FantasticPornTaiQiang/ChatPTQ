package repository.api

import page.setting.Billing
import page.setting.Cost
import repository.service.ApiService
import retrofit2.http.GET
import retrofit2.http.Query
import util.get99DaysAgoFormatDate
import util.getTodayFormatDate

interface AccountService : ApiService {
    @GET("dashboard/billing/subscription")
    suspend fun billSubscription(): Billing

    @GET("dashboard/billing/usage")
    suspend fun dailyCost(@Query("start_date") startDate: String = get99DaysAgoFormatDate(), @Query("end_date") endDate: String = getTodayFormatDate()): Cost

}
