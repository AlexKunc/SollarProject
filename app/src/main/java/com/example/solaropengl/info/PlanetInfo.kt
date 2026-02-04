package com.example.solaropengl.info

import androidx.annotation.DrawableRes
import com.example.solaropengl.R

data class PlanetInfo(
    val id: Int,
    val name: String,
    val description: String,
    @DrawableRes val imageRes: Int
)

object PlanetRepository {

    val planets: List<PlanetInfo> = listOf(
        PlanetInfo(
            id = 0,
            name = "Меркурий",
            description = "Самая близкая к Солнцу планета. Почти не имеет атмосферы, поэтому перепады температур огромные. Год на Меркурии длится 88 земных суток.",
            imageRes = R.drawable.mercury
        ),
        PlanetInfo(
            id = 1,
            name = "Венера",
            description = "Вторая планета от Солнца. Имеет плотную атмосферу из CO₂ и сильный парниковый эффект — одна из самых горячих планет. Вращается очень медленно и в обратную сторону.",
            imageRes = R.drawable.venus
        ),
        PlanetInfo(
            id = 2,
            name = "Земля",
            description = "Третья планета от Солнца и единственная известная планета с жизнью. Имеет жидкую воду на поверхности и магнитное поле, защищающее от солнечного ветра.",
            imageRes = R.drawable.earth
        ),
        PlanetInfo(
            id = 3,
            name = "Марс",
            description = "Красная планета. Атмосфера разреженная, в основном CO₂. На Марсе есть крупнейший вулкан Солнечной системы — Олимп, а также следы древней воды.",
            imageRes = R.drawable.mars
        ),
        PlanetInfo(
            id = 4,
            name = "Юпитер",
            description = "Самая большая планета Солнечной системы — газовый гигант. Известен Большим Красным Пятном (гигантским штормом). Имеет десятки спутников.",
            imageRes = R.drawable.jupiter
        ),
        PlanetInfo(
            id = 5,
            name = "Сатурн",
            description = "Газовый гигант с наиболее выраженной системой колец. Плотность настолько мала, что гипотетически мог бы плавать в воде. Имеет много спутников, включая Титан.",
            imageRes = R.drawable.saturn
        ),
        PlanetInfo(
            id = 6,
            name = "Уран",
            description = "Ледяной гигант. Ось вращения сильно наклонена — планета как будто «лежит на боку». Атмосфера содержит метан, придающий голубоватый оттенок.",
            imageRes = R.drawable.uranus
        ),
        PlanetInfo(
            id = 7,
            name = "Нептун",
            description = "Самая дальняя из крупных планет. Ледяной гигант с сильными ветрами и штормами. Из-за метана имеет насыщенный синий цвет.",
            imageRes = R.drawable.neptune
        )
    )

    fun byId(id: Int): PlanetInfo? = planets.firstOrNull { it.id == id }
}
