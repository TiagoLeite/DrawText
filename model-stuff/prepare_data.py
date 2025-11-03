#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script auxiliar para preparar dados EMNIST
Converte arquivos .gz para CSV ou baixa datasets
"""

import gzip
import numpy as np
import pandas as pd
import os
import urllib.request
import argparse
from pathlib import Path


def download_emnist_dataset(dataset_type='digits', output_dir='./data'):
    """
    Baixa dataset EMNIST do repositório oficial
    
    Args:
        dataset_type: 'digits' ou 'letters'
        output_dir: Diretório de saída
    """
    print(f"Baixando EMNIST {dataset_type}...")
    
    # Criar diretório se não existir
    Path(output_dir).mkdir(parents=True, exist_ok=True)
    
    # URLs base
    base_url = "http://www.itl.nist.gov/iaui/vip/cs_links/EMNIST/gzip.zip"
    
    print("\n⚠️  ATENÇÃO:")
    print("O download direto do EMNIST requer baixar o arquivo completo (561MB)")
    print("Recomendamos baixar manualmente de:")
    print("https://www.nist.gov/itl/products-and-services/emnist-dataset")
    print("\nDepois, use este script para converter os arquivos .gz para CSV:")
    print(f"  python prepare_data.py --convert --type {dataset_type}")
    

def convert_idx_to_csv(images_path, labels_path, output_path):
    """
    Converte arquivos IDX (formato MNIST) para CSV
    
    Args:
        images_path: Caminho para arquivo de imagens (.gz ou descompactado)
        labels_path: Caminho para arquivo de labels (.gz ou descompactado)
        output_path: Caminho para arquivo CSV de saída
    """
    print(f"\nConvertendo para CSV...")
    print(f"  Imagens: {images_path}")
    print(f"  Labels: {labels_path}")
    print(f"  Saída: {output_path}")
    
    # Função para abrir arquivo (com ou sem gzip)
    def open_file(path):
        if path.endswith('.gz'):
            return gzip.open(path, 'rb')
        else:
            return open(path, 'rb')
    
    # Ler labels
    with open_file(labels_path) as lbpath:
        # Pular header (8 bytes)
        lbpath.read(8)
        labels = np.frombuffer(lbpath.read(), dtype=np.uint8)
    
    print(f"  Labels carregados: {len(labels)}")
    
    # Ler imagens
    with open_file(images_path) as imgpath:
        # Pular header (16 bytes)
        imgpath.read(16)
        images = np.frombuffer(imgpath.read(), dtype=np.uint8)
        images = images.reshape(len(labels), 784)
    
    print(f"  Imagens carregadas: {images.shape}")
    
    # Combinar labels e imagens
    data = np.column_stack((labels, images))
    
    # Salvar como CSV
    print(f"  Salvando CSV...")
    df = pd.DataFrame(data)
    df.to_csv(output_path, index=False, header=False)
    
    print(f"✓ Arquivo salvo: {output_path}")
    print(f"  Tamanho: {os.path.getsize(output_path) / (1024*1024):.2f} MB")


def verify_csv_format(csv_path, expected_type='digits'):
    """
    Verifica se o arquivo CSV está no formato correto
    
    Args:
        csv_path: Caminho para arquivo CSV
        expected_type: 'digits' ou 'letters'
    """
    print(f"\nVerificando formato do arquivo: {csv_path}")
    
    # Ler primeiras linhas
    df = pd.read_csv(csv_path, header=None, nrows=10)
    
    print(f"  Shape: {df.shape}")
    print(f"  Colunas esperadas: 785 (1 label + 784 pixels)")
    
    if df.shape[1] != 785:
        print("  ❌ ERRO: Número incorreto de colunas!")
        return False
    
    # Verificar labels
    labels = df.iloc[:, 0].values
    print(f"  Labels encontrados: {np.unique(labels)}")
    
    if expected_type == 'digits':
        expected_labels = set(range(10))
    else:
        expected_labels = set(range(1, 27))  # EMNIST letters: 1-26
    
    found_labels = set(labels)
    if not found_labels.issubset(expected_labels):
        print(f"  ⚠️  AVISO: Labels inesperados encontrados")
    
    # Verificar valores de pixels
    pixels = df.iloc[:, 1:].values
    print(f"  Valores de pixels: min={pixels.min()}, max={pixels.max()}")
    
    if pixels.min() < 0 or pixels.max() > 255:
        print("  ❌ ERRO: Valores de pixels fora do intervalo [0, 255]!")
        return False
    
    print("  ✓ Formato parece correto!")
    return True


def create_sample_dataset(input_csv, output_csv, sample_size=10000):
    """
    Cria um dataset menor para testes rápidos
    
    Args:
        input_csv: Arquivo CSV completo
        output_csv: Arquivo CSV de saída (amostra)
        sample_size: Número de amostras
    """
    print(f"\nCriando dataset de amostra...")
    print(f"  Entrada: {input_csv}")
    print(f"  Saída: {output_csv}")
    print(f"  Tamanho: {sample_size} amostras")
    
    # Ler dataset completo
    df = pd.read_csv(input_csv, header=None)
    print(f"  Dataset original: {len(df)} amostras")
    
    # Fazer amostragem estratificada (manter proporção de classes)
    labels = df.iloc[:, 0]
    sample_df = df.groupby(labels, group_keys=False).apply(
        lambda x: x.sample(min(len(x), sample_size // len(labels.unique())))
    )
    
    # Embaralhar
    sample_df = sample_df.sample(frac=1).reset_index(drop=True)
    
    # Salvar
    sample_df.to_csv(output_csv, index=False, header=False)
    
    print(f"  ✓ Amostra criada: {len(sample_df)} amostras")
    print(f"  Tamanho: {os.path.getsize(output_csv) / (1024*1024):.2f} MB")


def main():
    parser = argparse.ArgumentParser(
        description='Preparar dados EMNIST para treinamento',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Exemplos de uso:

1. Converter arquivos .gz para CSV:
   python prepare_data.py --convert --type digits \\
       --train-images train-images-idx3-ubyte.gz \\
       --train-labels train-labels-idx1-ubyte.gz \\
       --test-images t10k-images-idx3-ubyte.gz \\
       --test-labels t10k-labels-idx1-ubyte.gz

2. Verificar formato de CSV:
   python prepare_data.py --verify \\
       --csv emnist-digits-train.csv --type digits

3. Criar dataset de amostra para testes:
   python prepare_data.py --sample \\
       --input emnist-digits-train.csv \\
       --output emnist-digits-train-sample.csv \\
       --size 10000
        """
    )
    
    parser.add_argument('--convert', action='store_true',
                       help='Converter arquivos IDX para CSV')
    parser.add_argument('--verify', action='store_true',
                       help='Verificar formato de arquivo CSV')
    parser.add_argument('--sample', action='store_true',
                       help='Criar dataset de amostra')
    parser.add_argument('--download', action='store_true',
                       help='Informações sobre download')
    
    parser.add_argument('--type', type=str, choices=['digits', 'letters'],
                       help='Tipo de dataset')
    
    # Para conversão
    parser.add_argument('--train-images', type=str,
                       help='Arquivo de imagens de treino')
    parser.add_argument('--train-labels', type=str,
                       help='Arquivo de labels de treino')
    parser.add_argument('--test-images', type=str,
                       help='Arquivo de imagens de teste')
    parser.add_argument('--test-labels', type=str,
                       help='Arquivo de labels de teste')
    parser.add_argument('--output-dir', type=str, default='.',
                       help='Diretório de saída')
    
    # Para verificação
    parser.add_argument('--csv', type=str,
                       help='Arquivo CSV para verificar')
    
    # Para amostragem
    parser.add_argument('--input', type=str,
                       help='Arquivo CSV de entrada')
    parser.add_argument('--output', type=str,
                       help='Arquivo CSV de saída')
    parser.add_argument('--size', type=int, default=10000,
                       help='Tamanho da amostra')
    
    args = parser.parse_args()
    
    if args.download:
        download_emnist_dataset(args.type or 'digits', args.output_dir)
    
    elif args.convert:
        if not all([args.train_images, args.train_labels, 
                   args.test_images, args.test_labels, args.type]):
            print("Erro: Para conversão, forneça todos os arquivos e o tipo")
            parser.print_help()
            return
        
        # Converter treino
        train_output = os.path.join(args.output_dir, 
                                   f'emnist-{args.type}-train.csv')
        convert_idx_to_csv(args.train_images, args.train_labels, train_output)
        
        # Converter teste
        test_output = os.path.join(args.output_dir,
                                  f'emnist-{args.type}-test.csv')
        convert_idx_to_csv(args.test_images, args.test_labels, test_output)
        
        print("\n✓ Conversão completa!")
        print(f"  Treino: {train_output}")
        print(f"  Teste: {test_output}")
    
    elif args.verify:
        if not args.csv or not args.type:
            print("Erro: Forneça --csv e --type")
            parser.print_help()
            return
        
        verify_csv_format(args.csv, args.type)
    
    elif args.sample:
        if not args.input or not args.output:
            print("Erro: Forneça --input e --output")
            parser.print_help()
            return
        
        create_sample_dataset(args.input, args.output, args.size)
    
    else:
        parser.print_help()


if __name__ == '__main__':
    main()
