package ma.ensaf.veryempty.data

import android.annotation.SuppressLint
import android.content.Context
import ma.ensaf.veryempty.models.CUsers
import ma.ensaf.veryempty.R
import java.util.*


// for testing with dummy data
object Constants {
    private val rnd = Random()
    @SuppressLint("Recycle")
    fun getUsers(ctx: Context): List<CUsers?> {
        val items: MutableList<CUsers?> = ArrayList()
        val names_arr = ctx.resources.getStringArray(R.array.people_names)
        val imgs_arr = ctx.resources.obtainTypedArray(R.array.people_photos)
        val locations_arr = ctx.resources.getStringArray(R.array.people_locations)
        val blood_groups_arr = ctx.resources.getStringArray(R.array.blood_groups)
        val dates_arr = ctx.resources.getStringArray(R.array.last_donation_dates)
        for (i in names_arr.indices) {
            val item = CUsers(
                i + 1,
                names_arr[i],
                imgs_arr.getResourceId(i, -1),
                getRandomValue(ctx, locations_arr),
                "+91 " + getRandomIndex(
                    rnd, 731234567, 732234567
                ).toString(),
                blood_groups_arr[i],
                dates_arr[i]
            )
            items.add(item)
        }
        Collections.shuffle(items, rnd)
        return items
    }

    private fun getRandomIndex(r: Random, min: Int, max: Int): Int {
        return r.nextInt(max - min) + min
    }

    private fun getRandomValue(ctx: Context, parsed_arr: Array<String>): String {
        return parsed_arr[getRandomIndex(
            rnd,
            0,
            parsed_arr.size - 1
        )]
    }

    // pass the case object clicked
    const val USER_EXTRA_OBJECT = "ma.ensaf.veryempty.models.Users"
}