package com.arnyminerz.paraulogic.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Scaffold { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Aplicació clausurada",
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Hola, em dic Arnau, i sóc un estudiant d'enginyeria de telecomunicacions que ja fa 6 mesos que he estat treballant en aquesta aplicació. Sabent-me molt malament, ha d'arribar a la seua fi.\nVaig iniciar aquest projecte amb tota la passió del món, perquè m'agradava el Paraulògic, a mi, i a la meua família, i volia poder tindre funcions extra de les quals no disposava al lloc web. Amb aquesta idea en ment, vaig decidir desenvolupar una nova aplicació que extendira la web, mantenint aquesta essència que fa tant especial al Paraulògic.\nHe treballat durant molts mesos desenfrenadament, fins a altes hores de la matinada, solucionant problemes, incorporant noves funcions, i intentant dur el Paraulògic encara més enllà.\nAvui ha arribat el dia que sabia que havia de passar; des de RodaMots m'han sol·licitat que retire l'aplicació, i sent conscient que la marca no n'és de la meua propietat, no tinc altra opció que obeïr.\nLamento moltíssim les molèsties, i retirar així de cop una aplicació, ja que sé que sou molts els que la feu servir, però no està en la meua mà aquesta decisió.\nSense res més a dir, i sense alternativa viable actualment, clausuro l'aplicació. Podeu seguir jugant a la web oficial, i el codi font seguirà estant present a GitHub. Gràcies per jugar, i fins la pròxima. Salut.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth(),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = {
                                Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse("https://paraulogic.cat"))
                                    .also { startActivity(it) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                        ) {
                            Text("Paraulògic")
                        }
                        FilledTonalButton(
                            onClick = {
                                Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse("https://github.com/Paraulogic/Android"))
                                    .also { startActivity(it) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp),
                        ) {
                            Text("GitHub")
                        }
                    }
                }
            }
        }
    }
}
