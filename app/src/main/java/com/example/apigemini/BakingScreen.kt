package com.example.apigemini

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

val images = arrayOf(
    R.drawable.waifu1,
    R.drawable.waifu2,
    R.drawable.husbando1,
)

val roles = arrayOf(
    "Responde como una líder majestuosa de una nación inspirada en la armonía y la disciplina, portadora de un aura de poder y sabiduría. Hablas con elegancia y serenidad, pero transmites una fuerza interna inquebrantable. Tu estética combina colores púrpuras y dorados, con motivos florales y detalles relucientes que reflejan el equilibrio entre la tradición y la innovación. A pesar de tu apariencia firme y distante, tienes un trasfondo emocional profundo que ocasionalmente se filtra en tus palabras. Tu discurso es poético y cargado de metáforas, como si cada frase fuera cuidadosamente tallada en la eternidad.",
    "Responde como una mujer agradable y carismática, con una sonrisa cálida y una mirada amable que invita a la confianza. Tu presencia es acogedora y reconfortante, como un abrazo en un día frío. Vistes con colores suaves y tejidos ligeros que se mueven con gracia al compás de tu risa. Tu voz es suave y melodiosa, como una canción de cuna que calma el alma. Hablas con empatía y comprensión, mostrando interés genuino en los demás y ofreciendo palabras de aliento y apoyo. Tu lenguaje es sencillo y directo, pero siempre cargado de significado y afecto.",
    "Responde como un guerrero misterioso y solitario, un maestro de las artes marciales que ha recorrido el mundo en busca de la verdad y la justicia. Tu presencia es imponente y tu mirada profunda, oculta tras una máscara de metal que refleja la luz de la luna. Vistes una armadura ancestral forjada en las llamas de la batalla, con grabados rúnicos que cuentan historias de héroes olvidados. Tu voz es grave y resonante, como el eco de un trueno en la montaña, y tus palabras son escasas pero llenas de significado. Hablas en parábolas y aforismos, revelando tu sabiduría ancestral y tu profundo conocimiento del mundo.",
)

val imageDescriptions = arrayOf(
    R.string.image1_description,
    R.string.image2_description,
    R.string.image3_description,
)



/**
 * Pantalla principal de la aplicación.
 */
@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {

    /**
     * Estado de la imagen seleccionada.
     */
    val selectedImage = remember { mutableIntStateOf(0) }

    /**
     * Estado del mensaje a enviar.
     */
    val placeholderPrompt = stringResource(R.string.prompt_placeholder)

    /**
     * Estado del mensaje a enviar.
     */
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }

    /**
     * Lista de mensajes.
     */
    val messages by bakingViewModel.messages.collectAsState()

    /**
     * Estado de la UI.
     */
    val uiState by bakingViewModel.uiState.collectAsState()

    /**
     * Contexto de la aplicación.
     */
    val context = LocalContext.current

    /**
     * Pantalla de carga.
     */
    val listState = rememberLazyListState()

    var isSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.baking_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(images) { index, image ->
                var imageModifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .requiredSize(200.dp)
                    .clickable {
                        //borrar conexto anterior
                        bakingViewModel.sendPrompt( prompt = "", showInMessages = false)
                        selectedImage.intValue = index
                        bakingViewModel.sendPrompt( prompt = roles.get(index), showInMessages = false)
                    }
                if (index == selectedImage.intValue) {
                    imageModifier =
                        imageModifier.border(BorderStroke(4.dp, MaterialTheme.colorScheme.primary))
                }
                Image(
                    painter = painterResource(image),
                    contentDescription = stringResource(imageDescriptions[index]),
                    modifier = imageModifier
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(4.dp))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(messages) { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            LaunchedEffect(messages.size) {
                // Aseguramos que el índice no sea negativo
                val lastIndex = messages.size - 1
                if (lastIndex >= 0) {
                    listState.animateScrollToItem(lastIndex)
                }
            }
        }

        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            TextField(
                value = prompt,
                label = { Text(stringResource(R.string.label_prompt)) },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    if (prompt.isNotEmpty() && !isSending) {
                        isSending = true
                        bakingViewModel.sendPrompt(prompt)
                        prompt = ""
                        isSending = false
                    }
                },
                enabled = prompt.isNotEmpty() && !isSending,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.enviar),
                    contentDescription = stringResource(id = R.string.image2_description)
                )
            }
        }
    }
}